package com.rayven0421.ashnotes.domain.model

data class BudgetSummary(
    val totalSpent: Long,
    val remaining: Long,
    val percentUsed: Float
)

data class CategorySpending(
    val categoryId: String,
    val name: String,
    val spentMinor: Long,
    val proportion: Float
)

object BudgetCalculator {
    fun calculateTotalSpent(expenses: List<Expense>): Long {
        return expenses.filter { it.deletedAt == null }.sumOf { it.amountMinor }
    }

    fun calculateCategorySpending(
        expenses: List<Expense>,
        categories: List<Category>
    ): List<CategorySpending> {
        val activeExpenses = expenses.filter { it.deletedAt == null }
        val totalSpent = calculateTotalSpent(activeExpenses)
        val categoryMap = categories.associateBy { it.id }

        val grouped = activeExpenses.groupBy { it.categoryId }
        val spendings = grouped.map { (categoryId, exps) ->
            val category = categoryMap[categoryId]
            val categoryName = category?.name ?: "Other"
            val spent = exps.sumOf { it.amountMinor }
            val proportion = if (totalSpent > 0L) {
                (spent.toDouble() / totalSpent.toDouble()).toFloat()
            } else {
                0.0f
            }
            CategorySpending(
                categoryId = categoryId,
                name = categoryName,
                spentMinor = spent,
                proportion = proportion
            )
        }

        return spendings.sortedWith(
            compareByDescending<CategorySpending> { it.spentMinor }
                .thenBy { it.name }
        )
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
