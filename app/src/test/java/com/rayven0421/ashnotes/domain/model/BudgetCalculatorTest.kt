package com.rayven0421.ashnotes.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetCalculatorTest {

    @Test
    fun zeroBudget_withZeroSpent_calculatesCorrectly() {
        val budget = Budget(
            workspaceId = "w1",
            name = "Zero Budget",
            amountMinor = 0L,
            startDate = 0L,
            endDate = 1000L
        )
        val summary = BudgetCalculator.calculateSummaryForBudget(budget, emptyList())
        assertEquals(0L, summary.totalSpent)
        assertEquals(0L, summary.remaining)
        assertEquals(0.0f, summary.percentUsed, 0.001f)
    }

    @Test
    fun zeroBudget_withSpent_calculatesSpentAndRemainingCorrectly() {
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
    fun overBudgetByOneMinorUnit_calculatesSpentAndRemainingCorrectly() {
        val budget = Budget(
            workspaceId = "w1",
            name = "Budget",
            amountMinor = 10000L,
            startDate = 0L,
            endDate = 1000L
        )
        val expenses = listOf(
            Expense(
                budgetId = budget.id,
                categoryId = "c1",
                amountMinor = 10001L,
                description = "Extra centavo item"
            )
        )

        val summary = BudgetCalculator.calculateSummaryForBudget(budget, expenses)
        assertEquals(10001L, summary.totalSpent)
        assertEquals(-1L, summary.remaining)
        assertEquals(1.0001f, summary.percentUsed, 0.0001f)
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
    fun largeValue_calculatesSpentAndRemainingCorrectly() {
        val budget = Budget(
            workspaceId = "w1",
            name = "Large Budget",
            amountMinor = 1_000_000_000_00L, // ₱1,000,000,000.00
            startDate = 0L,
            endDate = 1000L
        )
        val expenses = listOf(
            Expense(
                budgetId = budget.id,
                categoryId = "c1",
                amountMinor = 750_000_000_00L, // ₱750,000,000.00
                description = "Property purchase"
            )
        )

        val summary = BudgetCalculator.calculateSummaryForBudget(budget, expenses)
        assertEquals(750_000_000_00L, summary.totalSpent)
        assertEquals(250_000_000_00L, summary.remaining)
        assertEquals(0.75f, summary.percentUsed, 0.001f)
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

    @Test
    fun categorySpending_rankedOrderAndStableTies() {
        val categories = listOf(
            Category(id = "c1", name = "Zebra", icon = "tag", color = "#000000"),
            Category(id = "c2", name = "Food", icon = "tag", color = "#000000"),
            Category(id = "c3", name = "Bills", icon = "tag", color = "#000000")
        )
        val expenses = listOf(
            Expense(budgetId = "b1", categoryId = "c2", amountMinor = 5000L, description = "Groceries"),
            Expense(budgetId = "b1", categoryId = "c1", amountMinor = 2000L, description = "Zoo"),
            Expense(budgetId = "b1", categoryId = "c3", amountMinor = 5000L, description = "Electric")
        )

        val spending = BudgetCalculator.calculateCategorySpending(expenses, categories)
        assertEquals(3, spending.size)
        assertEquals("Bills", spending[0].name)
        assertEquals(5000L, spending[0].spentMinor)
        assertEquals("Food", spending[1].name)
        assertEquals(5000L, spending[1].spentMinor)
        assertEquals("Zebra", spending[2].name)
        assertEquals(2000L, spending[2].spentMinor)
    }

    @Test
    fun categorySpending_noExpenses_returnsEmpty() {
        val categories = listOf(
            Category(id = "c1", name = "Food", icon = "tag", color = "#000000")
        )
        val spending = BudgetCalculator.calculateCategorySpending(emptyList(), categories)
        assertEquals(0, spending.size)
    }

    @Test
    fun categorySpending_deletedRowsExcluded() {
        val categories = listOf(
            Category(id = "c1", name = "Food", icon = "tag", color = "#000000"),
            Category(id = "c2", name = "Transport", icon = "tag", color = "#000000")
        )
        val expenses = listOf(
            Expense(budgetId = "b1", categoryId = "c1", amountMinor = 3000L, description = "Lunch"),
            Expense(budgetId = "b1", categoryId = "c2", amountMinor = 4000L, description = "Taxi", deletedAt = 12345L)
        )

        val spending = BudgetCalculator.calculateCategorySpending(expenses, categories)
        assertEquals(1, spending.size)
        assertEquals("Food", spending[0].name)
        assertEquals(3000L, spending[0].spentMinor)
        assertEquals(1.0f, spending[0].proportion, 0.001f)
    }

    @Test
    fun calculateSummary_noDivisionByZero() {
        val summaryZero = BudgetCalculator.calculateSummary(0L, 0L)
        assertEquals(0.0f, summaryZero.percentUsed, 0.001f)

        val summaryOver = BudgetCalculator.calculateSummary(0L, 1000L)
        assertEquals(1.0f, summaryOver.percentUsed, 0.001f)
    }
}
