package com.rayven0421.ashnotes.feature.editor

import com.rayven0421.ashnotes.domain.model.Document

enum class SaveStatus {
    Saved,
    Saving,
    Error
}

data class EditorState(
    val noteId: String,
    val title: String,
    val document: Document,
    val saveStatus: SaveStatus = SaveStatus.Saved,
    val errorMessage: String? = null,
    val pendingFocusBlockId: String? = null,
    val pendingFocusCursorIndex: Int = 0,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)
