package com.rayven0421.ashnotes.feature.budget

import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.domain.model.Budget
import com.rayven0421.ashnotes.domain.model.Category
import com.rayven0421.ashnotes.domain.model.Expense
import com.rayven0421.ashnotes.domain.model.Workspace
import com.rayven0421.ashnotes.domain.repository.BudgetRepository
import com.rayven0421.ashnotes.domain.repository.CategoryRepository
import com.rayven0421.ashnotes.domain.repository.ExpenseRepository
import com.rayven0421.ashnotes.domain.repository.WorkspaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var budgetRepository: FakeBudgetRepository
    private lateinit var expenseRepository: FakeExpenseRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var workspaceRepository: FakeWorkspaceRepository
    private lateinit var viewModel: BudgetViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        budgetRepository = FakeBudgetRepository()
        expenseRepository = FakeExpenseRepository()
        categoryRepository = FakeCategoryRepository()
        workspaceRepository = FakeWorkspaceRepository()

        val budget = Budget(
            id = "b1",
            workspaceId = "w1",
            name = "Monthly Budget",
            amountMinor = 100000L,
            startDate = 0L,
            endDate = 1000L
        )
        budgetRepository.budgetsFlow.value = listOf(budget)

        val categories = listOf(
            Category(id = "c1", name = "Food", icon = "restaurant", color = "#875FFF"),
            Category(id = "c2", name = "Bills", icon = "receipt", color = "#FFB85F")
        )
        categoryRepository.categoriesFlow.value = categories

        viewModel = BudgetViewModel(
            budgetRepository,
            expenseRepository,
            categoryRepository,
            workspaceRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_calculatesTotalSpentFromNonDeletedExpenses() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        val expense1 = Expense(id = "e1", budgetId = "b1", categoryId = "c1", amountMinor = 15000L, description = "Groceries")
        val expense2 = Expense(id = "e2", budgetId = "b1", categoryId = "c2", amountMinor = 25000L, description = "Electricity")
        val deleted = Expense(id = "e3", budgetId = "b1", categoryId = "c1", amountMinor = 50000L, description = "Cancelled", deletedAt = 100L)
        expenseRepository.setExpenses(listOf(expense1, expense2, deleted))

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val content = (state as UiState.Success).data
        assertEquals(40000L, content.spentMinor)
        assertEquals(2, content.transactionGroups.sumOf { it.transactions.size })
        collectJob.cancel()
    }

    @Test
    fun addExpense_dialogStateAndSave() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.openAddExpenseDialog()
        assertTrue(viewModel.showExpenseDialog.value)
        assertNull(viewModel.editingExpense.value)

        viewModel.saveExpense(categoryId = "c1", amountMinor = 12500L, date = 500L, note = "Coffee")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.showExpenseDialog.value)
        val expenses = expenseRepository.expenses
        assertEquals(1, expenses.size)
        assertEquals(12500L, expenses.first().amountMinor)
        assertEquals("Coffee", expenses.first().description)
        collectJob.cancel()
    }

    @Test
    fun editExpense_updatesExistingExpense() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        val existing = Expense(id = "e1", budgetId = "b1", categoryId = "c1", amountMinor = 10000L, description = "Old")
        expenseRepository.setExpenses(listOf(existing))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.openEditExpenseDialog(existing)
        assertTrue(viewModel.showExpenseDialog.value)
        assertEquals(existing, viewModel.editingExpense.value)

        viewModel.saveExpense(categoryId = "c2", amountMinor = 15000L, date = 600L, note = "Updated")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.showExpenseDialog.value)
        assertNull(viewModel.editingExpense.value)

        val updated = expenseRepository.expenses.first { it.id == "e1" }
        assertEquals(15000L, updated.amountMinor)
        assertEquals("c2", updated.categoryId)
        assertEquals("Updated", updated.description)
        collectJob.cancel()
    }

    @Test
    fun deleteExpense_softDeletesAndEmitsSnackbarEventAndUndoRestores() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        val existing = Expense(id = "e1", budgetId = "b1", categoryId = "c1", amountMinor = 10000L, description = "Lunch")
        expenseRepository.setExpenses(listOf(existing))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.requestDeleteExpense(existing)
        assertEquals(existing, viewModel.showDeleteConfirmDialog.value)

        var emittedEvent: BudgetUiEvent? = null
        val eventJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { emittedEvent = it }
        }

        viewModel.confirmDeleteExpense()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.showDeleteConfirmDialog.value)
        val deletedInRepo = expenseRepository.expenses.first { it.id == "e1" }
        assertTrue(deletedInRepo.deletedAt != null)
        assertTrue(emittedEvent is BudgetUiEvent.ShowExpenseDeletedSnackbar)

        // Undo
        viewModel.undoDeleteExpense((emittedEvent as BudgetUiEvent.ShowExpenseDeletedSnackbar).expense)
        testDispatcher.scheduler.advanceUntilIdle()

        val restored = expenseRepository.expenses.first { it.id == "e1" }
        assertNull(restored.deletedAt)
        eventJob.cancel()
        collectJob.cancel()
    }

    @Test
    fun editBudget_updatesBudgetNameAndAmount() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.openEditBudgetDialog()
        assertTrue(viewModel.showEditDialog.value)

        viewModel.updateBudget(name = "Updated Budget", amountMinor = 150000L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.showEditDialog.value)
        val updatedBudget = budgetRepository.getBudgetById("b1")
        assertEquals("Updated Budget", updatedBudget?.name)
        assertEquals(150000L, updatedBudget?.amountMinor)

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val content = (state as UiState.Success).data
        assertEquals("Updated Budget", content.title)
        assertEquals(150000L, content.totalMinor)
        collectJob.cancel()
    }

    @Test
    fun pagination_paginatesExpensesAndChangesPages() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        val expensesList = (1..15).map { index ->
            Expense(id = "e$index", budgetId = "b1", categoryId = "c1", amountMinor = 1000L * index, description = "Exp $index", date = 1000L * index)
        }
        expenseRepository.setExpenses(expensesList)
        testDispatcher.scheduler.advanceUntilIdle()

        val statePage1 = viewModel.uiState.value
        assertTrue(statePage1 is UiState.Success)
        val content1 = (statePage1 as UiState.Success).data
        assertEquals(1, content1.currentPage)
        assertEquals(2, content1.totalPages)
        assertEquals(10, content1.transactionGroups.sumOf { it.transactions.size })

        viewModel.selectPage(2)
        testDispatcher.scheduler.advanceUntilIdle()

        val statePage2 = viewModel.uiState.value
        assertTrue(statePage2 is UiState.Success)
        val content2 = (statePage2 as UiState.Success).data
        assertEquals(2, content2.currentPage)
        assertEquals(2, content2.totalPages)
        assertEquals(5, content2.transactionGroups.sumOf { it.transactions.size })
        collectJob.cancel()
    }

    @Test
    fun addExpense_jumpsToPage1() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        val expensesList = (1..15).map { index ->
            Expense(id = "e$index", budgetId = "b1", categoryId = "c1", amountMinor = 1000L * index, description = "Exp $index", date = 1000L * index)
        }
        expenseRepository.setExpenses(expensesList)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectPage(2)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.currentPage.value)

        viewModel.openAddExpenseDialog()
        viewModel.saveExpense(categoryId = "c1", amountMinor = 5000L, date = 20000L, note = "New Expense")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.currentPage.value)
        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(1, (state as UiState.Success).data.currentPage)
        collectJob.cancel()
    }
}

class FakeBudgetRepository : BudgetRepository {
    val budgetsFlow = MutableStateFlow<List<Budget>>(emptyList())

    override fun observeBudgets(workspaceId: String): Flow<List<Budget>> {
        return budgetsFlow.map { list -> list.filter { it.workspaceId == workspaceId && it.deletedAt == null } }
    }

    override fun observeBudgetById(id: String): Flow<Budget?> {
        return budgetsFlow.map { list -> list.firstOrNull { it.id == id } }
    }

    override suspend fun getBudgetById(id: String): Budget? {
        return budgetsFlow.value.firstOrNull { it.id == id }
    }

    override fun observeTotalSpent(budgetId: String): Flow<Long> {
        return MutableStateFlow(0L)
    }

    override suspend fun getTotalSpent(budgetId: String): Long {
        return 0L
    }

    override suspend fun createBudget(budget: Budget): String {
        budgetsFlow.value = budgetsFlow.value + budget
        return budget.id
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetsFlow.value = budgetsFlow.value.map { if (it.id == budget.id) budget else it }
    }

    override suspend fun deleteBudget(id: String) {
        budgetsFlow.value = budgetsFlow.value.map { if (it.id == id) it.copy(deletedAt = System.currentTimeMillis()) else it }
    }
}

class FakeExpenseRepository : ExpenseRepository {
    val expenses = mutableListOf<Expense>()
    val expensesFlow = MutableStateFlow<List<Expense>>(emptyList())

    fun setExpenses(list: List<Expense>) {
        expenses.clear()
        expenses.addAll(list)
        expensesFlow.value = list
    }

    override fun observeExpenses(budgetId: String): Flow<List<Expense>> {
        return expensesFlow.map { list -> list.filter { it.budgetId == budgetId && it.deletedAt == null } }
    }

    override suspend fun getExpenseById(id: String): Expense? {
        return expenses.firstOrNull { it.id == id }
    }

    override suspend fun createExpense(expense: Expense): String {
        expenses.add(expense)
        expensesFlow.value = expenses.toList()
        return expense.id
    }

    override suspend fun updateExpense(expense: Expense) {
        val idx = expenses.indexOfFirst { it.id == expense.id }
        if (idx >= 0) {
            expenses[idx] = expense
        } else {
            expenses.add(expense)
        }
        expensesFlow.value = expenses.toList()
    }

    override suspend fun deleteExpense(id: String) {
        val existing = expenses.firstOrNull { it.id == id } ?: return
        val softDeleted = existing.copy(deletedAt = System.currentTimeMillis())
        updateExpense(softDeleted)
    }
}

class FakeCategoryRepository : CategoryRepository {
    val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())

    override fun observeCategories(): Flow<List<Category>> {
        return categoriesFlow
    }

    override suspend fun getCategoryById(id: String): Category? {
        return categoriesFlow.value.firstOrNull { it.id == id }
    }

    override suspend fun createCategory(category: Category): String {
        categoriesFlow.value = categoriesFlow.value + category
        return category.id
    }

    override suspend fun updateCategory(category: Category) {
        categoriesFlow.value = categoriesFlow.value.map { if (it.id == category.id) category else it }
    }

    override suspend fun deleteCategory(id: String) {
        categoriesFlow.value = categoriesFlow.value.map { if (it.id == id) it.copy(deletedAt = System.currentTimeMillis()) else it }
    }
}

class FakeWorkspaceRepository : WorkspaceRepository {
    override fun observeWorkspaces(): Flow<List<Workspace>> {
        return MutableStateFlow(listOf(Workspace(id = "w1", name = "Default", createdAt = 0L, updatedAt = 0L, deletedAt = null)))
    }

    override fun observeActiveWorkspaceId(): Flow<String> {
        return MutableStateFlow("w1")
    }

    override suspend fun getWorkspaceById(id: String): Workspace? {
        return Workspace(id = "w1", name = "Default", createdAt = 0L, updatedAt = 0L, deletedAt = null)
    }

    override suspend fun createWorkspace(name: String): String {
        return "w1"
    }

    override suspend fun updateWorkspace(workspace: Workspace) {
    }

    override suspend fun deleteWorkspace(id: String) {
    }

    override suspend fun clearAllLocalData() {
    }
}
