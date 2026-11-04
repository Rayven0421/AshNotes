package com.rayven0421.ashnotes.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetCalculatorTest {

    @Test
    fun zeroBudget_calculatesSpentAndRemainingCorrectly() {
        val budget = Budget(
            workspaceId = "w1",
            name = "Zero Budget",
            amountMinor = 0L,
            startDate = 0L,
            endDate = 1000L
        )
        val expenses = listOf(
            Expense(
                budgetId = budget.id,
                categoryId = "c1",
                amountMinor = 1500L,
                description = "Coffee"
            )
        )

        val summary = BudgetCalculator.calculateSummaryForBudget(budget, expenses)
        assertEquals(1500L, summary.totalSpent)
        assertEquals(-1500L, summary.remaining)
        assertEquals(1.0f, summary.percentUsed, 0.001f)
    }

    @Test
    fun exactBudget_calculatesSpentAndRemainingCorrectly() {
        val budget = Budget(
            workspaceId = "w1",
            name = "Exact Budget",
            amountMinor = 10000L,
            startDate = 0L,
            endDate = 1000L
        )
        val expenses = listOf(
            Expense(
                budgetId = budget.id,
                categoryId = "c1",
                amountMinor = 6000L,
                description = "Dinner"
            ),
            Expense(
                budgetId = budget.id,
                categoryId = "c2",
                amountMinor = 4000L,
                description = "Taxi"
            )
        )

        val summary = BudgetCalculator.calculateSummaryForBudget(budget, expenses)
        assertEquals(10000L, summary.totalSpent)
        assertEquals(0L, summary.remaining)
        assertEquals(1.0f, summary.percentUsed, 0.001f)
    }

    @Test
    fun overBudget_calculatesSpentAndRemainingCorrectly() {
        val budget = Budget(
            workspaceId = "w1",
            name = "Over Budget",
            amountMinor = 5000L,
            startDate = 0L,
            endDate = 1000L
        )
        val expenses = listOf(
            Expense(
                budgetId = budget.id,
                categoryId = "c1",
                amountMinor = 8000L,
                description = "Shopping"
            )
        )

        val summary = BudgetCalculator.calculateSummaryForBudget(budget, expenses)
        assertEquals(8000L, summary.totalSpent)
        assertEquals(-3000L, summary.remaining)
        assertEquals(1.6f, summary.percentUsed, 0.001f)
    }

    @Test
    fun deletedExpense_doesNotCountInTotalSpent() {
        val budget = Budget(
            workspaceId = "w1",
            name = "Active Budget",
            amountMinor = 10000L,
            startDate = 0L,
            endDate = 1000L
        )
        val activeExpense = Expense(
            budgetId = budget.id,
            categoryId = "c1",
            amountMinor = 3000L,
            description = "Lunch"
        )
        val deletedExpense = Expense(
            budgetId = budget.id,
            categoryId = "c1",
            amountMinor = 5000L,
            description = "Refunded item",
            deletedAt = 123456789L
        )

        val summary = BudgetCalculator.calculateSummaryForBudget(budget, listOf(activeExpense, deletedExpense))
        assertEquals(3000L, summary.totalSpent)
        assertEquals(7000L, summary.remaining)
        assertEquals(0.3f, summary.percentUsed, 0.001f)
    }
}
