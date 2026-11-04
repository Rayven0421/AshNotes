package com.rayven0421.ashnotes.feature.notes.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.feature.notes.NoteItem
import com.rayven0421.ashnotes.feature.notes.NotesContent

@Composable
fun NotesContentArea(
    uiState: UiState<NotesContent>,
    selectedIds: Set<String>,
    isSelectionMode: Boolean,
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onNewNoteClick: () -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleArchive: (String) -> Unit,
    onRenameRequest: (NoteItem) -> Unit,
    onDeleteRequest: (NoteItem) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is UiState.Empty, is UiState.Loading -> {
            NotesEmptyState(onNewNoteClick = onNewNoteClick, modifier = modifier)
        }
        is UiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AshNotesTheme.colors.over
                )
            }
        }
        is UiState.Success -> {
            val content = uiState.data
            if (content.pinnedNotes.isEmpty() && content.otherNotes.isEmpty()) {
                NotesEmptyState(onNewNoteClick = onNewNoteClick, modifier = modifier)
            } else {
                NotesList(
                    pinned = content.pinnedNotes,
                    others = content.otherNotes,
                    selectedIds = selectedIds,
                    isSelectionMode = isSelectionMode,
                    onNoteClick = onNoteClick,
                    onNoteLongClick = onNoteLongClick,
                    onToggleSelect = onToggleSelect,
                    onTogglePin = onTogglePin,
                    onToggleArchive = onToggleArchive,
                    onRenameRequest = onRenameRequest,
                    onDeleteRequest = onDeleteRequest,
                    modifier = modifier
                )
            }
        }
    }
}
