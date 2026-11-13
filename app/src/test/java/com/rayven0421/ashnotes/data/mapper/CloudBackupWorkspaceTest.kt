package com.rayven0421.ashnotes.data.mapper

import com.rayven0421.ashnotes.data.local.entity.BudgetEntity
import com.rayven0421.ashnotes.data.local.entity.NoteEntity
import com.rayven0421.ashnotes.data.local.entity.WorkspaceEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudBackupWorkspaceTest {

    @Test
    fun restoreMergeDecision_filtersWorkspacesCorrectly() {
        val remoteWorkspaces = listOf(
            WorkspaceEntity(id = "w1", name = "Personal"),
            WorkspaceEntity(id = "w2", name = "Work")
        )
        val localIds = setOf("w1")

        val newWorkspaces = RestoreMergeDecision.filterNewItems(remoteWorkspaces, localIds) { it.id }

        assertEquals(1, newWorkspaces.size)
        assertEquals("w2", newWorkspaces[0].id)
    }

    @Test
    fun noteAndBudgetToMap_carriesWorkspaceId() {
        val note = NoteEntity(id = "n1", workspaceId = "w1", title = "Test Note")
        val budget = BudgetEntity(id = "b1", workspaceId = "w1", name = "Test Budget", amountMinor = 5000L, startDate = 0L, endDate = 0L)

        val noteMap = note.toMap()
        val budgetMap = budget.toMap()

        assertTrue(noteMap.containsKey("workspaceId"))
        assertEquals("w1", noteMap["workspaceId"])

        assertTrue(budgetMap.containsKey("workspaceId"))
        assertEquals("w1", budgetMap["workspaceId"])

        val workspace = WorkspaceEntity(id = "w1", name = "Personal")
        val workspaceMap = workspace.toMap()
        val restoredWorkspace = workspaceEntityFromMap(workspaceMap)

        assertEquals("w1", restoredWorkspace.id)
        assertEquals("Personal", restoredWorkspace.name)
    }
}
