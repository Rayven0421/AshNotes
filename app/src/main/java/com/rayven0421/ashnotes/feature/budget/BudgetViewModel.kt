package com.rayven0421.ashnotes.feature.budget

import androidx.lifecycle.ViewModel
import com.rayven0421.ashnotes.core.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BudgetViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<BudgetContent>>(UiState.Empty)
    val uiState: StateFlow<UiState<BudgetContent>> = _uiState.asStateFlow()

    fun createBudget() {
        // Future budget creation handling in Phase 4
    }
}

data class BudgetContent(
    val title: String,
    val totalMinor: Long,
    val spentMinor: Long,
    val daysLeft: Int,
    val paceFraction: Float,
    val categories: List<CategorySpendingItem>,
    val transactionGroups: List<TransactionGroup>
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
    val categoryName: String,
    val description: String,
    val amountMinor: Long
)
