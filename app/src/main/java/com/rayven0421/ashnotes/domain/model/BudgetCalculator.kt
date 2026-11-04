package com.rayven0421.ashnotes.domain.model

data class BudgetSummary(
    val totalSpent: Long,
    val remaining: Long,
    val percentUsed: Float
)

object BudgetCalculator {
    fun calculateTotalSpent(expenses: List<Expense>): Long {
        return expenses.filter { it.deletedAt == null }.sumOf { it.amountMinor }
    }

    fun calculateSummary(budgetAmountMinor: Long, totalSpent: Long): BudgetSummary {
        val remaining = budgetAmountMinor - totalSpent
        val percentUsed = if (budgetAmountMinor == 0L) {
            if (totalSpent > 0L) 1.0f else 0.0f
        } else {
            (totalSpent.toDouble() / budgetAmountMinor.toDouble()).toFloat()
        }
        return BudgetSummary(
            totalSpent = totalSpent,
            remaining = remaining,
            percentUsed = percentUsed
        )
    }

    fun calculateSummaryForBudget(budget: Budget, expenses: List<Expense>): BudgetSummary {
        val totalSpent = calculateTotalSpent(expenses)
        return calculateSummary(budget.amountMinor, totalSpent)
    }
}
