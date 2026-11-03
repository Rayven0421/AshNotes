package com.rayven0421.ashnotes.feature.budget

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme

internal object SampleBudgetData {
    val successState = BudgetContent(
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
                    TransactionItem("t1", "Dining & Coffee", "Artisan Coffee Roasters", 45000L),
                    TransactionItem("t2", "Groceries & Food", "Organic Supermarket", 125000L)
                )
            ),
            TransactionGroup(
                dateLabel = "Yesterday",
                transactions = listOf(
                    TransactionItem("t3", "Utilities & Internet", "Fiber Internet Bill", 280000L)
                )
            )
        )
    )

    val overBudgetState = BudgetContent(
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
                    TransactionItem("t1", "Groceries & Food", "Bulk Wholesale Grocery", 320000L)
                )
            )
        )
    )

    val zeroBudgetState = BudgetContent(
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
