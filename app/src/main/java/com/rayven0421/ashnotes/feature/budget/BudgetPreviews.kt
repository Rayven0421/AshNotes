package com.rayven0421.ashnotes.feature.budget

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.domain.model.Expense

internal object SampleBudgetData {
    private const val now = 1700000000000L

    val successState = BudgetContent(
        budgetId = "b1",
        title = "Monthly Budget",
        totalMinor = 500000L,
        spentMinor = 325000L,
        daysLeft = 12,
        paceFraction = 0.6f,
        categories = listOf(
            CategorySpendingItem("c1", "Groceries & Food", 145000L, 0.446f),
            CategorySpendingItem("c2", "Utilities & Internet", 98000L, 0.301f),
            CategorySpendingItem("c3", "Dining & Coffee", 52000L, 0.160f),
            CategorySpendingItem("c4", "Transport", 30000L, 0.093f)
        ),
        transactionGroups = listOf(
            TransactionGroup(
                dateLabel = "Today",
                transactions = listOf(
                    TransactionItem(
                        id = "t1",
                        categoryId = "c3",
                        categoryName = "Dining & Coffee",
                        description = "Artisan Coffee Roasters",
                        amountMinor = 45000L,
                        date = now,
                        expense = Expense(id = "t1", budgetId = "b1", categoryId = "c3", amountMinor = 45000L, description = "Artisan Coffee Roasters", date = now)
                    ),
                    TransactionItem(
                        id = "t2",
                        categoryId = "c1",
                        categoryName = "Groceries & Food",
                        description = "Organic Supermarket",
                        amountMinor = 125000L,
                        date = now,
                        expense = Expense(id = "t2", budgetId = "b1", categoryId = "c1", amountMinor = 125000L, description = "Organic Supermarket", date = now)
                    )
                )
            ),
            TransactionGroup(
                dateLabel = "Yesterday",
                transactions = listOf(
                    TransactionItem(
                        id = "t3",
                        categoryId = "c2",
                        categoryName = "Utilities & Internet",
                        description = "Fiber Internet Bill",
                        amountMinor = 280000L,
                        date = now - 86400000L,
                        expense = Expense(id = "t3", budgetId = "b1", categoryId = "c2", amountMinor = 280000L, description = "Fiber Internet Bill", date = now - 86400000L)
                    )
                )
            )
        )
    )

    val overBudgetState = BudgetContent(
        budgetId = "b1",
        title = "Monthly Budget",
        totalMinor = 500000L,
        spentMinor = 532000L,
        daysLeft = 5,
        paceFraction = 0.85f,
        categories = listOf(
            CategorySpendingItem("c1", "Groceries & Food", 245000L, 0.460f),
            CategorySpendingItem("c2", "Utilities & Internet", 198000L, 0.372f),
            CategorySpendingItem("c3", "Dining & Coffee", 89000L, 0.168f)
        ),
        transactionGroups = listOf(
            TransactionGroup(
                dateLabel = "Today",
                transactions = listOf(
                    TransactionItem(
                        id = "t1",
                        categoryId = "c1",
                        categoryName = "Groceries & Food",
                        description = "Bulk Wholesale Grocery",
                        amountMinor = 320000L,
                        date = now,
                        expense = Expense(id = "t1", budgetId = "b1", categoryId = "c1", amountMinor = 320000L, description = "Bulk Wholesale Grocery", date = now)
                    )
                )
            )
        )
    )

    val zeroBudgetState = BudgetContent(
        budgetId = "b1",
        title = "Unset Budget",
        totalMinor = 0L,
        spentMinor = 0L,
        daysLeft = 0,
        paceFraction = 0f,
        categories = emptyList(),
        transactionGroups = emptyList()
    )
}

@Preview(name = "Budget - Success", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun BudgetScreenSuccessPreview() {
    AshNotesTheme {
        BudgetScreen(uiState = UiState.Success(SampleBudgetData.successState))
    }
}

@Preview(name = "Budget - Empty", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun BudgetScreenEmptyPreview() {
    AshNotesTheme {
        BudgetScreen(uiState = UiState.Empty)
    }
}

@Preview(name = "Budget - Over Budget", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun BudgetScreenOverBudgetPreview() {
    AshNotesTheme {
        BudgetScreen(uiState = UiState.Success(SampleBudgetData.overBudgetState))
    }
}

@Preview(name = "Budget - Zero Budget", showBackground = true, backgroundColor = 0xFF121116)
@Composable
fun BudgetScreenZeroBudgetPreview() {
    AshNotesTheme {
        BudgetScreen(uiState = UiState.Success(SampleBudgetData.zeroBudgetState))
    }
}
