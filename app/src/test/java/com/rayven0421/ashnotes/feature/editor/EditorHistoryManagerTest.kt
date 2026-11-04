package com.rayven0421.ashnotes.feature.editor

import com.rayven0421.ashnotes.domain.model.Block
import com.rayven0421.ashnotes.domain.model.Document
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EditorHistoryManagerTest {

    private lateinit var historyManager: EditorHistoryManager

    @Before
    fun setUp() {
        historyManager = EditorHistoryManager(maxHistory = 50)
    }

    private fun createDoc(vararg texts: String): Document {
        val blocks = texts.mapIndexed { index, text ->
            Block.Paragraph(
                id = "b_$index",
                position = index,
                createdAt = 1000L + index,
                updatedAt = 1000L + index,
                text = text
            )
        }
        return Document(version = 1, blocks = blocks)
    }

    @Test
    fun pushSnapshot_andUndoRedo_restoresStateAndFocusInfo() {
        val doc1 = createDoc("State 1")
        val doc2 = createDoc("State 2")

        val entry1 = HistoryEntry(document = doc1, title = "Title 1", focusedBlockId = "b_0", cursorIndex = 7)
        val entry2 = HistoryEntry(document = doc2, title = "Title 2", focusedBlockId = "b_0", cursorIndex = 7)

        historyManager.pushSnapshot(entry1)
        assertTrue(historyManager.canUndo)
        assertFalse(historyManager.canRedo)

        val restoredUndo = historyManager.undo(entry2)
        assertEquals(entry1, restoredUndo)
        assertEquals("b_0", restoredUndo?.focusedBlockId)
        assertEquals(7, restoredUndo?.cursorIndex)
        assertEquals("Title 1", restoredUndo?.title)

        assertTrue(historyManager.canRedo)

        val restoredRedo = historyManager.redo(entry1)
        assertEquals(entry2, restoredRedo)
        assertEquals("b_0", restoredRedo?.focusedBlockId)
        assertEquals(7, restoredRedo?.cursorIndex)
        assertEquals("Title 2", restoredRedo?.title)
    }

    @Test
    fun redoStack_clearedByNewEdit() {
        val doc1 = createDoc("1")
        val doc2 = createDoc("2")
        val doc3 = createDoc("3")

        val entry1 = HistoryEntry(doc1, "T1", "b_0", 1)
        val entry2 = HistoryEntry(doc2, "T2", "b_0", 1)
        val entry3 = HistoryEntry(doc3, "T3", "b_0", 1)

        historyManager.pushSnapshot(entry1)
        historyManager.undo(entry2)

        assertTrue(historyManager.canRedo)

        // New edit happens
        historyManager.pushSnapshot(entry3)

        assertFalse("Redo stack must be cleared after a new edit snapshot", historyManager.canRedo)
        assertNull(historyManager.redo(entry3))
    }

    @Test
    fun historyStack_cappedAtMaxHistoryOf50() {
        for (i in 1..60) {
            val doc = createDoc("Text $i")
            val entry = HistoryEntry(doc, "Title $i", "b_0", i)
            historyManager.pushSnapshot(entry)
        }

        assertEquals(50, historyManager.undoSize)

        // Undo 50 times should be possible
        var undoCount = 0
        var current = HistoryEntry(createDoc("Current"), "Current Title", "b_0", 0)
        while (historyManager.canUndo) {
            val prev = historyManager.undo(current)
            if (prev != null) {
                current = prev
                undoCount++
            }
        }

        assertEquals(50, undoCount)
        assertFalse(historyManager.canUndo)
        // Oldest preserved entry should be #11 (since 1..10 were popped off due to cap of 50)
        assertEquals("Title 11", current.title)
    }

    @Test
    fun duplicateSnapshot_isNotPushed() {
        val doc1 = createDoc("Same")
        val entry1 = HistoryEntry(doc1, "Title", "b_0", 4)

        historyManager.pushSnapshot(entry1)
        historyManager.pushSnapshot(entry1)

        assertEquals(1, historyManager.undoSize)
    }
}
