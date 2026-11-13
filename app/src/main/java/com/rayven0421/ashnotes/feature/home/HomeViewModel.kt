package com.rayven0421.ashnotes.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.common.calculatePeriodMetrics
import com.rayven0421.ashnotes.core.common.formatNoteDate
import com.rayven0421.ashnotes.domain.model.Budget
import com.rayven0421.ashnotes.domain.model.BudgetCalculator
import com.rayven0421.ashnotes.domain.model.Category
import com.rayven0421.ashnotes.domain.model.Expense
import com.rayven0421.ashnotes.domain.repository.AuthRepository
import com.rayven0421.ashnotes.domain.repository.BudgetRepository
import com.rayven0421.ashnotes.domain.repository.CategoryRepository
import com.rayven0421.ashnotes.domain.repository.ExpenseRepository
import com.rayven0421.ashnotes.domain.repository.NoteRepository
import com.rayven0421.ashnotes.domain.repository.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _errorMessageResId = MutableStateFlow<Int?>(null)
    val errorMessageResId: StateFlow<Int?> = _errorMessageResId.asStateFlow()

    val uiState: StateFlow<UiState<HomeContent>> = combine(
        workspaceRepository.activeWorkspaceId,
        workspaceRepository.workspaces
    ) { activeId, workspaces ->
        val activeWs = workspaces.firstOrNull { it.id == activeId }
        Pair(activeId, activeWs?.name ?: "Personal")
    }
        .flatMapLatest { (workspaceId, activeWorkspaceName) ->
            combine(
                noteRepository.observeNotes(workspaceId),
                budgetRepository.observeBudgets(workspaceId),
                categoryRepository.observeCategories(),
                authRepository.currentUser
            ) { notes, budgets, categories, currentUser ->
                Quintuple(notes, budgets, categories, currentUser, activeWorkspaceName)
            }
        }
        .flatMapLatest { (notes, budgets, categories, currentUser, activeWorkspaceName) ->
            val activeBudget = budgets.firstOrNull { !it.isArchived }
            if (activeBudget == null) {
                flowOf(Sextuple(notes, null as Budget?, emptyList<Expense>(), categories, currentUser?.email, activeWorkspaceName))
            } else {
                expenseRepository.observeExpenses(activeBudget.id).map { expenses ->
                    Sextuple(notes, activeBudget, expenses, categories, currentUser?.email, activeWorkspaceName)
                }
            }
        }
        .map { (notes, activeBudget, expenses, categories, userEmail, activeWorkspaceName) ->
            val recentNotes = notes
                .filter { !it.isArchived }
                .sortedByDescending { it.updatedAt }
                .take(3)
                .map { note ->
                    HomeNoteItem(
                        id = note.id,
                        title = note.title,
                        preview = note.preview,
                        updatedTime = formatNoteDate(note.updatedAt)
                    )
                }

            val categoryMap = categories.associateBy { it.id }
            val recentExpenses = expenses
                .take(3)
                .map { expense ->
                    val cat = categoryMap[expense.categoryId]
                    val title = expense.description.ifBlank { cat?.name ?: "Other" }
                    HomeExpenseItem(
                        id = expense.id,
                        title = title,
                        amountMinor = expense.amountMinor,
                        dateText = formatNoteDate(expense.date)
                    )
                }

            val hasBudget = activeBudget != null
            val spentMinor = if (activeBudget != null) {
                BudgetCalculator.calculateTotalSpent(expenses)
            } else {
                0L
            }
            val totalMinor = activeBudget?.amountMinor ?: 0L
            val paceFraction = if (activeBudget != null) {
                calculatePeriodMetrics(
                    activeBudget.startDate,
                    activeBudget.endDate
                ).second
            } else {
                0f
            }

            UiState.Success(
                HomeContent(
                    greetingResId = getGreetingByTimeOfDayResId(),
                    spentMinor = spentMinor,
                    totalMinor = totalMinor,
                    paceFraction = paceFraction,
                    hasBudget = hasBudget,
                    notes = recentNotes,
                    recentExpenses = recentExpenses,
                    userEmail = userEmail,
                    activeWorkspaceName = activeWorkspaceName
                )
            ) as UiState<HomeContent>
        }
        .catch { throwable ->
            emit(UiState.Error(throwable.message ?: "", throwable) as UiState<HomeContent>)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    fun createNewNote(title: String, onNoteCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val activeWorkspaceId = workspaceRepository.observeActiveWorkspaceId().first()
                val id = noteRepository.createNote(workspaceId = activeWorkspaceId, title = title.trim())
                onNoteCreated(id)
            } catch (_: Exception) {
                _errorMessageResId.value = R.string.error_failed_to_create_note
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessageResId.value = null
    }

    private fun getGreetingByTimeOfDayResId(): Int {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> R.string.home_greeting_morning
            in 12..17 -> R.string.home_greeting_afternoon
            else -> R.string.home_greeting_evening
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

private data class Sextuple<A, B, C, D, E, F>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F
)

data class HomeContent(
    val greetingResId: Int,
    val spentMinor: Long,
    val totalMinor: Long,
    val paceFraction: Float,
    val hasBudget: Boolean,
    val notes: List<HomeNoteItem>,
    val recentExpenses: List<HomeExpenseItem> = emptyList(),
    val userEmail: String?,
    val activeWorkspaceName: String = "Personal"
)

data class HomeNoteItem(
    val id: String,
    val title: String,
    val preview: String,
    val updatedTime: String
)

data class HomeExpenseItem(
    val id: String,
    val title: String,
    val amountMinor: Long,
    val dateText: String
)
