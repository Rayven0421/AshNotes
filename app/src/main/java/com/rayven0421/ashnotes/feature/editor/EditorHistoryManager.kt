package com.rayven0421.ashnotes.feature.editor

import com.rayven0421.ashnotes.domain.model.Document

data class HistoryEntry(
    val document: Document,
    val title: String,
    val focusedBlockId: String? = null,
    val cursorIndex: Int = 0
)

class EditorHistoryManager(
    private val maxHistory: Int = 50
) {
    private val undoStack = ArrayDeque<HistoryEntry>()
    private val redoStack = ArrayDeque<HistoryEntry>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
    val undoSize: Int get() = undoStack.size
    val redoSize: Int get() = redoStack.size

    fun pushSnapshot(entry: HistoryEntry) {
        if (undoStack.lastOrNull() == entry) return
        undoStack.addLast(entry)
        if (undoStack.size > maxHistory) {
            undoStack.removeFirst()
        }
        redoStack.clear()
    }

    fun undo(currentEntry: HistoryEntry): HistoryEntry? {
        if (undoStack.isEmpty()) return null
        redoStack.addLast(currentEntry)
        return undoStack.removeLast()
    }

    fun redo(currentEntry: HistoryEntry): HistoryEntry? {
        if (redoStack.isEmpty()) return null
        undoStack.addLast(currentEntry)
        return redoStack.removeLast()
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
