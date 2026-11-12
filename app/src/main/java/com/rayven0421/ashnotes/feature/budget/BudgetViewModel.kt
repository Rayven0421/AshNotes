package com.rayven0421.ashnotes.feature.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayven0421.ashnotes.core.common.BUDGET_PAGE_SIZE
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.common.calculatePeriodMetrics
import com.rayven0421.ashnotes.core.common.formatTransactionDateHeader
import com.rayven0421.ashnotes.core.common.getCurrentMonthPeriod
import com.rayven0421.ashnotes.core.common.paginate
import com.rayven0421.ashnotes.domain.model.Budget
import com.rayven0421.ashnotes.domain.model.BudgetCalculator
import com.rayven0421.ashnotes.domain.model.Category
import com.rayven0421.ashnotes.domain.model.Expense
import com.rayven0421.ashnotes.domain.repository.BudgetRepository
import com.rayven0421.ashnotes.domain.repository.CategoryRepository
import com.rayven0421.ashnotes.domain.repository.ExpenseRepository
import com.rayven0421.ashnotes.domain.repository.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BudgetUiEvent {
    data class ShowExpenseDeletedSnackbar(val expense: Expense) : BudgetUiEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _showExpenseDialog = MutableStateFlow(false)
    val showExpenseDialog: StateFlow<Boolean> = _showExpenseDialog.asStateFlow()

    private val _editingExpense = MutableStateFlow<Expense?>(null)
    val editingExpense: StateFlow<Expense?> = _editingExpense.asStateFlow()

    private val _showDeleteConfirmDialog = MutableStateFlow<Expense?>(null)
    val showDeleteConfirmDialog: StateFlow<Expense?> = _showDeleteConfirmDialog.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _uiEvent = MutableSharedFlow<BudgetUiEvent>()
    val uiEvent: SharedFlow<BudgetUiEvent> = _uiEvent.asSharedFlow()

    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<UiState<BudgetContent>> = workspaceRepository.observeActiveWorkspaceId()
        .flatMapLatest { workspaceId ->
            budgetRepository.observeBudgets(workspaceId)
        }
        .flatMapLatest { budgets ->
            val activeBudget = budgets.firstOrNull { !it.isArchived }
            if (activeBudget == null) {
                flowOf(UiState.Empty)
            } else {
                combine(
                    expenseRepository.observeExpenses(activeBudget.id),
                    categoryRepository.observeCategories(),
                    _currentPage
                ) { expenses, categoryList, page ->
                    val categoryMap = categoryList.associateBy { it.id }
                    val activeExpenses = expenses.filter { it.deletedAt == null }
                    val totalSpent = BudgetCalculator.calculateTotalSpent(activeExpenses)
                    val (daysLeft, paceFraction) = calculatePeriodMetrics(
                        activeBudget.startDate,
                        activeBudget.endDate
                    )

                    val sortedExpenses = activeExpenses.sortedByDescending { it.date }
                    val pagination = paginate(sortedExpenses, page, BUDGET_PAGE_SIZE)

                    val grouped = pagination.items.groupBy { expense ->
                        formatTransactionDateHeader(expense.date)
                    }

                    val transactionGroups = grouped.map { (dateLabel, expenseList) ->
                        TransactionGroup(
                            dateLabel = dateLabel,
                            transactions = expenseList.map { exp ->
                                val cat = categoryMap[exp.categoryId]
                                TransactionItem(
                                    id = exp.id,
                                    categoryId = exp.categoryId,
                                    categoryName = cat?.name ?: "Other",
                                    description = exp.description,
                                    amountMinor = exp.amountMinor,
                                    date = exp.date,
                                    expense = exp
                                )
                            }
                        )
                    }

                    val categorySpendings = BudgetCalculator.calculateCategorySpending(
                        expenses = activeExpenses,
                        categories = categoryList
                    )
                    val categoryItems = categorySpendings.map { cs ->
                        CategorySpendingItem(
                            categoryId = cs.categoryId,
                            name = cs.name,
                            spentMinor = cs.spentMinor,
                            proportion = cs.proportion
                        )
                    }

                    UiState.Success(
                        BudgetContent(
                            budgetId = activeBudget.id,
                            title = activeBudget.name,
                            totalMinor = activeBudget.amountMinor,
                            spentMinor = totalSpent,
                            daysLeft = daysLeft,
                            paceFraction = paceFraction,
                            categories = categoryItems,
                            transactionGroups = transactionGroups,
                            currentPage = pagination.currentPage,
                            totalPages = pagination.totalPages
                        )
                    ) as UiState<BudgetContent>
                }
            }
        }
        .catch { throwable ->
            emit(UiState.Error(throwable.message ?: "", throwable))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    fun openCreateBudgetDialog() {
        _showCreateDialog.value = true
    }

    fun dismissCreateBudgetDialog() {
        _showCreateDialog.value = false
    }

    fun createBudget(name: String, amountMinor: Long) {
        viewModelScope.launch {
            try {
                val activeWorkspaceId = workspaceRepository.observeActiveWorkspaceId().first()
                val (startDate, endDate) = getCurrentMonthPeriod()
                val budget = Budget(
                    workspaceId = activeWorkspaceId,
                    name = name.ifBlank { "Monthly Budget" },
                    amountMinor = amountMinor,
                    currencyCode = "PHP",
                    startDate = startDate,
                    endDate = endDate
                )
                budgetRepository.createBudget(budget)
                _showCreateDialog.value = false
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    fun openEditBudgetDialog() {
        _showEditDialog.value = true
    }

    fun dismissEditBudgetDialog() {
        _showEditDialog.value = false
    }

    fun updateBudget(name: String, amountMinor: Long) {
        viewModelScope.launch {
            try {
                val state = uiState.value
                val budgetId = if (state is UiState.Success) state.data.budgetId else return@launch
                val activeBudget = budgetRepository.getBudgetById(budgetId) ?: return@launch
                val updated = activeBudget.copy(
                    name = name.ifBlank { "Monthly Budget" },
                    amountMinor = amountMinor,
                    updatedAt = System.currentTimeMillis()
                )
                budgetRepository.updateBudget(updated)
                _showEditDialog.value = false
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    fun selectPage(page: Int) {
        _currentPage.value = page
    }

    fun openAddExpenseDialog() {
        _editingExpense.value = null
        _showExpenseDialog.value = true
    }

    fun openEditExpenseDialog(expense: Expense) {
        _editingExpense.value = expense
        _showExpenseDialog.value = true
    }

    fun dismissExpenseDialog() {
        _showExpenseDialog.value = false
        _editingExpense.value = null
    }

    fun saveExpense(categoryId: String, amountMinor: Long, date: Long, note: String) {
        viewModelScope.launch {
            try {
                val state = uiState.value
                val budgetId = if (state is UiState.Success) state.data.budgetId else return@launch
                val editing = _editingExpense.value

                if (editing == null) {
                    val expense = Expense(
                        budgetId = budgetId,
                        categoryId = categoryId,
                        amountMinor = amountMinor,
                        description = note,
                        date = date
                    )
                    expenseRepository.createExpense(expense)
                    _currentPage.value = 1
                } else {
                    val updated = editing.copy(
                        categoryId = categoryId,
                        amountMinor = amountMinor,
                        description = note,
                        date = date,
                        updatedAt = System.currentTimeMillis()
                    )
                    expenseRepository.updateExpense(updated)
                }
                _showExpenseDialog.value = false
                _editingExpense.value = null
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    fun requestDeleteExpense(expense: Expense) {
        _showExpenseDialog.value = false
        _editingExpense.value = null
        _showDeleteConfirmDialog.value = expense
    }

    fun dismissDeleteConfirmDialog() {
        _showDeleteConfirmDialog.value = null
    }

    fun confirmDeleteExpense() {
        val expenseToDelete = _showDeleteConfirmDialog.value ?: return
        viewModelScope.launch {
            try {
                expenseRepository.deleteExpense(expenseToDelete.id)
                _showDeleteConfirmDialog.value = null
                _uiEvent.emit(BudgetUiEvent.ShowExpenseDeletedSnackbar(expenseToDelete))
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    fun undoDeleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                expenseRepository.updateExpense(
                    expense.copy(deletedAt = null, updatedAt = System.currentTimeMillis())
                )
            } catch (_: Exception) {
                // Ignore
            }
        }
    }
}

data class BudgetContent(
    val budgetId: String = "",
    val title: String,
    val totalMinor: Long,
    val spentMinor: Long,
    val daysLeft: Int,
    val paceFraction: Float,
    val categories: List<CategorySpendingItem>,
    val transactionGroups: List<TransactionGroup>,
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

data class CategorySpendingItem(
    val categoryId: String,
    val name: String,
    val spentMinor: Long,
    val proportion: Float
)

data class TransactionGroup(
    val dateLabel: String,
    val transactions: List<TransactionItem>
)

data class TransactionItem(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val description: String,
    val amountMinor: Long,
    val date: Long,
    val expense: Expense
)
