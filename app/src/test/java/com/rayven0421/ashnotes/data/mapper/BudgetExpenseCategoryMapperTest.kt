package com.rayven0421.ashnotes.data.mapper

import com.rayven0421.ashnotes.data.local.entity.BudgetEntity
import com.rayven0421.ashnotes.data.local.entity.CategoryEntity
import com.rayven0421.ashnotes.data.local.entity.ExpenseEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetExpenseCategoryMapperTest {

    @Test
    fun budgetEntityToDomain_mapsCorrectly() {
        val entity = BudgetEntity(
            id = "b1",
            workspaceId = "w1",
            name = "Monthly Budget",
            amountMinor = 500000L,
            currencyCode = "PHP",
            startDate = 1000L,
            endDate = 2000L,
            isArchived = false,
            createdAt = 100L,
            updatedAt = 200L,
            deletedAt = null
        )
        val domain = entity.toDomain()
        assertEquals("b1", domain.id)
        assertEquals("w1", domain.workspaceId)
        assertEquals("Monthly Budget", domain.name)
        assertEquals(500000L, domain.amountMinor)
        assertEquals("PHP", domain.currencyCode)
        assertEquals(1000L, domain.startDate)
        assertEquals(2000L, domain.endDate)
        assertEquals(false, domain.isArchived)
        assertEquals(100L, domain.createdAt)
        assertEquals(200L, domain.updatedAt)
        assertEquals(null, domain.deletedAt)

        val backToEntity = domain.toEntity()
        assertEquals(entity, backToEntity)
    }

    @Test
    fun expenseEntityToDomain_mapsCorrectly() {
        val entity = ExpenseEntity(
            id = "e1",
            budgetId = "b1",
            categoryId = "c1",
            amountMinor = 15000L,
            currencyCode = "PHP",
            description = "Groceries",
            date = 1500L,
            createdAt = 100L,
            updatedAt = 200L,
            deletedAt = null
        )
        val domain = entity.toDomain()
        assertEquals("e1", domain.id)
        assertEquals("b1", domain.budgetId)
        assertEquals("c1", domain.categoryId)
        assertEquals(15000L, domain.amountMinor)
        assertEquals("PHP", domain.currencyCode)
        assertEquals("Groceries", domain.description)
        assertEquals(1500L, domain.date)
        assertEquals(100L, domain.createdAt)
        assertEquals(200L, domain.updatedAt)
        assertEquals(null, domain.deletedAt)

        val backToEntity = domain.toEntity()
        assertEquals(entity, backToEntity)
    }

    @Test
    fun categoryEntityToDomain_mapsCorrectly() {
        val entity = CategoryEntity(
            id = "c1",
            name = "Food",
            icon = "restaurant",
            color = "#FF0000",
            isDefault = true,
            createdAt = 100L,
            updatedAt = 200L,
            deletedAt = null
        )
        val domain = entity.toDomain()
        assertEquals("c1", domain.id)
        assertEquals("Food", domain.name)
        assertEquals("restaurant", domain.icon)
        assertEquals("#FF0000", domain.color)
        assertEquals(true, domain.isDefault)
        assertEquals(100L, domain.createdAt)
        assertEquals(200L, domain.updatedAt)
        assertEquals(null, domain.deletedAt)

        val backToEntity = domain.toEntity()
        assertEquals(entity, backToEntity)
    }
}
