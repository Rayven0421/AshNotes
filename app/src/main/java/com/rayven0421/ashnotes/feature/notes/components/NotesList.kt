package com.rayven0421.ashnotes.feature.notes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.feature.notes.NoteItem

@Composable
fun NotesList(
    pinned: List<NoteItem>,
    others: List<NoteItem>,
    selectedIds: Set<String>,
    isSelectionMode: Boolean,
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleArchive: (String) -> Unit,
    onRenameRequest: (NoteItem) -> Unit,
    onDeleteRequest: (NoteItem) -> Unit,
    modifier: Modifier = Modifier,
    isArchivedView: Boolean = false
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = AshNotesTheme.spacing.listBottomPadding),
        verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.none)
    ) {
        if (isArchivedView) {
            item {
                Text(
                    text = stringResource(R.string.notes_view_archived),
                    style = MaterialTheme.typography.labelMedium,
                    color = AshNotesTheme.colors.textMuted,
                    modifier = Modifier.padding(bottom = AshNotesTheme.spacing.small)
                )
            }
        }
        if (pinned.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.notes_pinned_header),
                    style = MaterialTheme.typography.titleMedium,
                    color = AshNotesTheme.colors.textMuted,
                    modifier = Modifier.padding(bottom = AshNotesTheme.spacing.small)
                )
            }
            itemsIndexed(
                items = pinned,
                key = { _, note -> note.id }
            ) { index, note ->
                val isSelected = note.id in selectedIds
                NoteRow(
                    note = note,
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode,
                    onClick = {
                        if (isSelectionMode) {
                            onToggleSelect(note.id)
                        } else {
                            onNoteClick(note.id)
                        }
                    },
                    onLongClick = {
                        onNoteLongClick(note.id)
                    },
                    onTogglePin = onTogglePin,
                    onToggleArchive = onToggleArchive,
                    onRenameRequest = { onRenameRequest(note) },
                    onDeleteRequest = { onDeleteRequest(note) }
                )
                if (index < pinned.size - 1 || others.isNotEmpty()) {
                    HorizontalDivider(
                        color = AshNotesTheme.colors.line,
                        thickness = AshNotesTheme.spacing.hairline
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(AshNotesTheme.spacing.large))
            }
        }

        if (others.isNotEmpty()) {
            if (pinned.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.notes_other_header),
                        style = MaterialTheme.typography.titleMedium,
                        color = AshNotesTheme.colors.textMuted,
                        modifier = Modifier.padding(bottom = AshNotesTheme.spacing.small)
                    )
                }
            }
            itemsIndexed(
                items = others,
                key = { _, note -> note.id }
            ) { index, note ->
                val isSelected = note.id in selectedIds
                NoteRow(
                    note = note,
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode,
                    onClick = {
                        if (isSelectionMode) {
                            onToggleSelect(note.id)
                        } else {
                            onNoteClick(note.id)
                        }
                    },
                    onLongClick = {
                        onNoteLongClick(note.id)
                    },
                    onTogglePin = onTogglePin,
                    onToggleArchive = onToggleArchive,
                    onRenameRequest = { onRenameRequest(note) },
                    onDeleteRequest = { onDeleteRequest(note) }
                )
                if (index < others.size - 1) {
                    HorizontalDivider(
                        color = AshNotesTheme.colors.line,
                        thickness = AshNotesTheme.spacing.hairline
                    )
                }
            }
        }
    }
}
