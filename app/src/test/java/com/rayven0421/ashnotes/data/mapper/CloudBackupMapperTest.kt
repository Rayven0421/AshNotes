package com.rayven0421.ashnotes.data.mapper

import com.rayven0421.ashnotes.data.local.entity.BudgetEntity
import com.rayven0421.ashnotes.data.local.entity.CategoryEntity
import com.rayven0421.ashnotes.data.local.entity.ExpenseEntity
import com.rayven0421.ashnotes.data.local.entity.NoteEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudBackupMapperTest {

    @Test
    fun noteEntityToMapAndBack_withNullDeletedAt_preservesData() {
        val original = NoteEntity(
            id = "note-1",
            workspaceId = "ws-1",
            title = "Test Note",
            contentJson = "{\"blocks\":[]}",
            documentVersion = 2,
            isPinned = true,
            isArchived = false,
            createdAt = 1000L,
            updatedAt = 2000L,
            deletedAt = null
        )

        val map = original.toMap()
        val restored = noteEntityFromMap(map)

        assertEquals(original.id, restored.id)
        assertEquals(original.workspaceId, restored.workspaceId)
        assertEquals(original.title, restored.title)
        assertEquals(original.contentJson, restored.contentJson)
        assertEquals(original.documentVersion, restored.documentVersion)
        assertEquals(original.isPinned, restored.isPinned)
        assertEquals(original.isArchived, restored.isArchived)
        assertEquals(original.createdAt, restored.createdAt)
        assertEquals(original.updatedAt, restored.updatedAt)
        assertNull(restored.deletedAt)
    }

    @Test
    fun noteEntityToMapAndBack_withNonNullDeletedAt_preservesData() {
        val original = NoteEntity(
            id = "note-2",
            workspaceId = "ws-1",
            title = "Deleted Note",
            contentJson = "{}",
            documentVersion = 1,
            isPinned = false,
            isArchived = true,
            createdAt = 1000L,
            updatedAt = 2000L,
            deletedAt = 3000L
        )

        val map = original.toMap()
        val restored = noteEntityFromMap(map)

        assertEquals(3000L, restored.deletedAt)
    }

    @Test
    fun budgetEntityToMapAndBack_withLongMoneyAndNullDeletedAt_preservesData() {
        val original = BudgetEntity(
            id = "budget-1",
            workspaceId = "ws-1",
            name = "Monthly Budget",
            amountMinor = 15000L, // ₱150.00
            currencyCode = "PHP",
            startDate = 1000L,
            endDate = 5000L,
            isArchived = false,
            createdAt = 1000L,
            updatedAt = 2000L,
            deletedAt = null
        )

        val map = original.toMap()
        val restored = budgetEntityFromMap(map)

        assertEquals(original.id, restored.id)
        assertEquals(original.workspaceId, restored.workspaceId)
        assertEquals(original.name, restored.name)
        assertEquals(15000L, restored.amountMinor)
        assertEquals(original.currencyCode, restored.currencyCode)
        assertEquals(original.startDate, restored.startDate)
        assertEquals(original.endDate, restored.endDate)
        assertEquals(original.isArchived, restored.isArchived)
        assertNull(restored.deletedAt)
    }

    @Test
    fun expenseEntityToMapAndBack_withLongMoneyAndNullDeletedAt_preservesData() {
        val original = ExpenseEntity(
            id = "expense-1",
            budgetId = "budget-1",
            categoryId = "cat-1",
            amountMinor = 2500L, // ₱25.00
            currencyCode = "PHP",
            description = "Coffee",
            date = 1500L,
            createdAt = 1000L,
            updatedAt = 2000L,
            deletedAt = null
        )

        val map = original.toMap()
        val restored = expenseEntityFromMap(map)

        assertEquals(original.id, restored.id)
        assertEquals(original.budgetId, restored.budgetId)
        assertEquals(original.categoryId, restored.categoryId)
        assertEquals(2500L, restored.amountMinor)
        assertEquals(original.currencyCode, restored.currencyCode)
        assertEquals(original.description, restored.description)
        assertEquals(original.date, restored.date)
        assertNull(restored.deletedAt)
    }

    @Test
    fun categoryEntityToMapAndBack_withNullDeletedAt_preservesData() {
        val original = CategoryEntity(
            id = "cat-1",
            name = "Food",
            icon = "fastfood",
            color = "#FF0000",
            isDefault = true,
            createdAt = 1000L,
            updatedAt = 2000L,
            deletedAt = null
        )

        val map = original.toMap()
        val restored = categoryEntityFromMap(map)

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.icon, restored.icon)
        assertEquals(original.color, restored.color)
        assertEquals(original.isDefault, restored.isDefault)
        assertNull(restored.deletedAt)
    }

    @Test
    fun restoreMergeDecision_filtersExistingLocalIds() {
        val remoteNotes = listOf(
            NoteEntity(id = "n1", workspaceId = "w1", title = "N1"),
            NoteEntity(id = "n2", workspaceId = "w1", title = "N2"),
            NoteEntity(id = "n3", workspaceId = "w1", title = "N3")
        )

        // Local has n1 (even if soft deleted) and n4
        val existingLocalIds = setOf("n1", "n4")

        val toInsert = RestoreMergeDecision.filterNewItems(remoteNotes, existingLocalIds) { it.id }

        assertEquals(2, toInsert.size)
        assertEquals(listOf("n2", "n3"), toInsert.map { it.id })
    }

    @Test
    fun noteSizeChecker_detectsLargeNotes() {
        val smallContent = "a".repeat(100)
        assertFalse(NoteSizeChecker.isTooLarge(smallContent))

        // 901 KB content (> 900 KB threshold)
        val largeContent = "a".repeat(901 * 1024)
        assertTrue(NoteSizeChecker.isTooLarge(largeContent))
    }
}
