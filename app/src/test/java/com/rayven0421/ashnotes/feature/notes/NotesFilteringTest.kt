package com.rayven0421.ashnotes.feature.notes

import org.junit.Assert.assertEquals
import org.junit.Test

class NotesFilteringTest {

    @Test
    fun testNotesFilteringLogic_pinnedAndArchivedSeparation() {
        val notes = listOf(
            NoteItem(id = "1", title = "Active Pinned", preview = "", updatedTime = "", isPinned = true, isArchived = false),
            NoteItem(id = "2", title = "Active Other", preview = "", updatedTime = "", isPinned = false, isArchived = false),
            NoteItem(id = "3", title = "Archived Pinned", preview = "", updatedTime = "", isPinned = true, isArchived = true),
            NoteItem(id = "4", title = "Archived Other", preview = "", updatedTime = "", isPinned = false, isArchived = true)
        )

        val activeNotes = notes.filter { !it.isArchived }
        val archivedNotes = notes.filter { it.isArchived }

        assertEquals(2, activeNotes.size)
        assertEquals(2, archivedNotes.size)

        val activePinned = activeNotes.filter { it.isPinned }
        val activeOthers = activeNotes.filter { !it.isPinned }

        assertEquals(1, activePinned.size)
        assertEquals("1", activePinned[0].id)
        assertEquals(1, activeOthers.size)
        assertEquals("2", activeOthers[0].id)

        val archivedPinned = archivedNotes.filter { it.isPinned }
        val archivedOthers = archivedNotes.filter { !it.isPinned }

        assertEquals(1, archivedPinned.size)
        assertEquals("3", archivedPinned[0].id)
        assertEquals(1, archivedOthers.size)
        assertEquals("4", archivedOthers[0].id)
    }
}
