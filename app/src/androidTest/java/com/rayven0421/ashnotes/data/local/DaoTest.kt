package com.rayven0421.ashnotes.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rayven0421.ashnotes.data.local.entity.BudgetEntity
import com.rayven0421.ashnotes.data.local.entity.CategoryEntity
import com.rayven0421.ashnotes.data.local.entity.ExpenseEntity
import com.rayven0421.ashnotes.data.local.entity.NoteEntity
import com.rayven0421.ashnotes.data.local.entity.WorkspaceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoTest {

    private lateinit var database: AshNotesDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AshNotesDatabase::class.java
        ).allowMainThreadQueries().build()

        runBlocking {
            database.workspaceDao().insertWorkspace(
                WorkspaceEntity(id = "w1", name = "Test Workspace", createdAt = 1000L, updatedAt = 1000L)
            )
        }
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun noteDao_insert_readFlow_softDelete_restore_bulkOps() = runBlocking {
        val noteDao = database.noteDao()
        val initialTime = 100000L

        // 1. Insert & Read through Flow
        val note = NoteEntity(
            id = "n1",
            workspaceId = "w1",
            title = "Test Note",
            createdAt = initialTime,
            updatedAt = initialTime
        )
        noteDao.insertNote(note)

        val notesFlowList = noteDao.observeNotesByUpdated("w1").first()
        assertEquals(1, notesFlowList.size)
        assertEquals("Test Note", notesFlowList[0].title)

        // 2. Soft-delete hides the row
        noteDao.softDeleteNote("n1", deletedAt = 200000L)
        val hiddenList = noteDao.observeNotesByUpdated("w1").first()
        assertTrue(hiddenList.isEmpty())
        assertNull(noteDao.getNoteById("n1"))

        // 3. Restore brings it back
        noteDao.restoreNote("n1")
        val restoredList = noteDao.observeNotesByUpdated("w1").first()
        assertEquals(1, restoredList.size)
        assertNotNull(noteDao.getNoteById("n1"))

        // 4. Bulk pin/archive/delete don't change updatedAt
        val note2 = NoteEntity(
            id = "n2",
            workspaceId = "w1",
            title = "Second Note",
            createdAt = initialTime,
            updatedAt = initialTime
        )
        noteDao.insertNote(note2)

        val ids = listOf("n1", "n2")

        // Bulk Pin
        noteDao.bulkPinNotes(ids, true)
        var fetchedN1 = noteDao.getNoteById("n1")
        var fetchedN2 = noteDao.getNoteById("n2")
        assertTrue(fetchedN1!!.isPinned)
        assertTrue(fetchedN2!!.isPinned)
        assertEquals(initialTime, fetchedN1.updatedAt)
        assertEquals(initialTime, fetchedN2.updatedAt)

        // Bulk Archive
        noteDao.bulkArchiveNotes(ids, true)
        fetchedN1 = noteDao.getNoteById("n1")
        fetchedN2 = noteDao.getNoteById("n2")
        assertTrue(fetchedN1!!.isArchived)
        assertTrue(fetchedN2!!.isArchived)
        assertEquals(initialTime, fetchedN1.updatedAt)
        assertEquals(initialTime, fetchedN2.updatedAt)

        // Bulk Soft Delete
        noteDao.bulkSoftDeleteNotes(ids, deletedAt = 300000L)
        val deletedN1 = database.openHelper.readableDatabase.query(
            "SELECT * FROM notes WHERE id = 'n1'"
        )
        deletedN1.moveToFirst()
        val updatedAtIndex = deletedN1.getColumnIndex("updatedAt")
        assertEquals(initialTime, deletedN1.getLong(updatedAtIndex))
        deletedN1.close()

        // Bulk Restore
        noteDao.bulkRestoreNotes(ids)
        fetchedN1 = noteDao.getNoteById("n1")
        fetchedN2 = noteDao.getNoteById("n2")
        assertEquals(initialTime, fetchedN1!!.updatedAt)
        assertEquals(initialTime, fetchedN2!!.updatedAt)
    }

    @Test
    fun budgetDao_insert_readFlow_softDelete() = runBlocking {
        val budgetDao = database.budgetDao()
        val budget = BudgetEntity(
            id = "b1",
            workspaceId = "w1",
            name = "Food Budget",
            amountMinor = 50000L,
            startDate = 1000L,
            endDate = 2000L
        )
        budgetDao.insertBudget(budget)

        val list = budgetDao.observeBudgets("w1").first()
        assertEquals(1, list.size)
        assertEquals("Food Budget", list[0].name)

        budgetDao.softDeleteBudget("b1", deletedAt = 3000L)
        val hiddenList = budgetDao.observeBudgets("w1").first()
        assertTrue(hiddenList.isEmpty())
        assertNull(budgetDao.getBudgetById("b1"))
    }

    @Test
    fun expenseDao_insert_readFlow_softDelete_totalSpent() = runBlocking {
        val expenseDao = database.expenseDao()
        val expense = ExpenseEntity(
            id = "e1",
            budgetId = "b1",
            categoryId = "c1",
            amountMinor = 1500L,
            description = "Coffee"
        )
        expenseDao.insertExpense(expense)

        val list = expenseDao.observeExpenses("b1").first()
        assertEquals(1, list.size)
        assertEquals(1500L, expenseDao.getTotalSpent("b1"))

        expenseDao.softDeleteExpense("e1", deletedAt = 2000L)
        val hiddenList = expenseDao.observeExpenses("b1").first()
        assertTrue(hiddenList.isEmpty())
        assertNull(expenseDao.getExpenseById("e1"))
        assertEquals(0L, expenseDao.getTotalSpent("b1"))
    }

    @Test
    fun categoryDao_insert_readFlow_softDelete() = runBlocking {
        val categoryDao = database.categoryDao()
        val category = CategoryEntity(
            id = "c1",
            name = "Snacks",
            icon = "fastfood",
            color = "#FF0000"
        )
        categoryDao.insertCategory(category)

        val list = categoryDao.observeCategories().first()
        assertEquals(1, list.size)
        assertEquals("Snacks", list[0].name)

        categoryDao.softDeleteCategory("c1", deletedAt = 2000L)
        val hiddenList = categoryDao.observeCategories().first()
        assertTrue(hiddenList.isEmpty())
        assertNull(categoryDao.getCategoryById("c1"))
    }

    @Test
    fun clearAllLocalData_recreatesWorkspaceAndDefaultCategories() = runBlocking {
        val note = NoteEntity(id = "n1", workspaceId = "w1", title = "To be deleted")
        database.noteDao().insertNote(note)

        database.clearAllLocalData()

        val notes = database.noteDao().observeNotesByUpdated("w1").first()
        assertTrue(notes.isEmpty())

        val workspaces = database.workspaceDao().observeWorkspaces().first()
        assertEquals(1, workspaces.size)
        assertEquals("default_workspace", workspaces[0].id)
        assertEquals("Personal", workspaces[0].name)

        val categories = database.categoryDao().observeCategories().first()
        assertTrue(categories.isNotEmpty())
    }
}
