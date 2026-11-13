package com.rayven0421.ashnotes.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkspaceValidationTest {

    private val defaultWs = Workspace("default_workspace", "Personal", 1000L, 1000L, null)
    private val workWs = Workspace("w2", "Work", 2000L, 2000L, null)
    private val deletedWs = Workspace("w3", "Old Workspace", 3000L, 3000L, 4000L)

    @Test
    fun fallbackFunction_returnsDefault_whenStoredIdNullOrMissingOrDeleted() {
        val list = listOf(defaultWs, workWs, deletedWs)

        assertEquals("default_workspace", resolveActiveWorkspaceId(null, list))
        assertEquals("default_workspace", resolveActiveWorkspaceId("non_existent_id", list))
        assertEquals("default_workspace", resolveActiveWorkspaceId("w3", list))
    }

    @Test
    fun fallbackFunction_returnsStoredId_whenValidAndNonDeleted() {
        val list = listOf(defaultWs, workWs, deletedWs)

        assertEquals("w2", resolveActiveWorkspaceId("w2", list))
        assertEquals("default_workspace", resolveActiveWorkspaceId("default_workspace", list))
    }

    @Test
    fun nameValidation_returnsEmpty_whenBlank() {
        val list = listOf(defaultWs, workWs)

        assertEquals(WorkspaceNameValidationError.Empty, validateWorkspaceName("", list))
        assertEquals(WorkspaceNameValidationError.Empty, validateWorkspaceName("   ", list))
    }

    @Test
    fun nameValidation_returnsTooLong_whenExceeds40Chars() {
        val list = listOf(defaultWs, workWs)
        val longName = "A".repeat(41)

        assertEquals(WorkspaceNameValidationError.TooLong, validateWorkspaceName(longName, list))
    }

    @Test
    fun nameValidation_returnsDuplicate_whenCaseInsensitiveMatchExists() {
        val list = listOf(defaultWs, workWs, deletedWs)

        assertEquals(WorkspaceNameValidationError.Duplicate, validateWorkspaceName("personal", list))
        assertEquals(WorkspaceNameValidationError.Duplicate, validateWorkspaceName("WORK", list))
    }

    @Test
    fun nameValidation_returnsNull_whenValid() {
        val list = listOf(defaultWs, workWs, deletedWs)

        assertNull(validateWorkspaceName("Projects", list))
        assertNull(validateWorkspaceName("Old Workspace", list)) // deleted workspace name can be reused
    }
}
