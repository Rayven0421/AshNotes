package com.rayven0421.ashnotes.feature.notes.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.dialog.AshConfirmDialog
import com.rayven0421.ashnotes.core.designsystem.dialog.AshTextInputDialog
import com.rayven0421.ashnotes.feature.notes.NotesContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NotesDialogs(
    uiState: UiState<NotesContent>,
    showNewNoteDialog: Boolean,
    onDismissNewNote: () -> Unit,
    onConfirmNewNote: (String) -> Unit,
    noteIdToRename: String?,
    onDismissRename: () -> Unit,
    onConfirmRename: (String, String) -> Unit,
    noteIdToDelete: String?,
    onDismissDelete: () -> Unit,
    onConfirmDelete: (String) -> Unit,
    showBulkDeleteDialog: Boolean,
    bulkDeleteCount: Int,
    onDismissBulkDelete: () -> Unit,
    onConfirmBulkDelete: ((List<String>) -> Unit) -> Unit,
    onRestore: (String) -> Unit,
    onBulkRestore: (List<String>) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val deletedMsg = stringResource(R.string.snackbar_note_deleted)
    val undoLabel = stringResource(R.string.action_undo)

    // New Note Dialog
    if (showNewNoteDialog) {
        AshTextInputDialog(
            title = stringResource(R.string.dialog_new_note_title),
            label = stringResource(R.string.field_note_title_label),
            confirmLabel = stringResource(R.string.action_create),
            blankFallbackValue = stringResource(R.string.note_title_untitled),
            onConfirm = { title ->
                onConfirmNewNote(title)
                onDismissNewNote()
            },
            onDismiss = onDismissNewNote
        )
    }

    // Rename Dialog
    if (noteIdToRename != null) {
        val currentTitle = (uiState as? UiState.Success)?.data?.let { content ->
            (content.pinnedNotes + content.otherNotes).find { it.id == noteIdToRename }?.title
        } ?: ""

        AshTextInputDialog(
            title = stringResource(R.string.dialog_rename_title),
            label = stringResource(R.string.field_note_title_label),
            initialValue = currentTitle,
            confirmLabel = stringResource(R.string.action_save),
            blankFallbackValue = currentTitle, // Blank input keeps the old title
            onConfirm = { newTitle ->
                onConfirmRename(noteIdToRename, newTitle)
                onDismissRename()
            },
            onDismiss = onDismissRename
        )
    }

    // Single Delete Confirmation Dialog
    if (noteIdToDelete != null) {
        AshConfirmDialog(
            title = stringResource(R.string.dialog_delete_title),
            message = stringResource(R.string.dialog_delete_message),
            confirmLabel = stringResource(R.string.action_delete),
            isDestructive = true,
            onConfirm = {
                val id = noteIdToDelete
                onConfirmDelete(id)
                onDismissDelete()
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = deletedMsg,
                        actionLabel = undoLabel
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        onRestore(id)
                    }
                }
            },
            onDismiss = onDismissDelete
        )
    }

    // Bulk Delete Confirmation Dialog
    if (showBulkDeleteDialog && bulkDeleteCount > 0) {
        val bulkDeleteTitle = pluralStringResource(R.plurals.dialog_bulk_delete_title, bulkDeleteCount, bulkDeleteCount)
        val bulkDeletedMsg = pluralStringResource(R.plurals.snackbar_bulk_deleted, bulkDeleteCount, bulkDeleteCount)

        AshConfirmDialog(
            title = bulkDeleteTitle,
            message = stringResource(R.string.dialog_bulk_delete_message),
            confirmLabel = stringResource(R.string.action_delete),
            isDestructive = true,
            onConfirm = {
                onConfirmBulkDelete { deletedIds ->
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = bulkDeletedMsg,
                            actionLabel = undoLabel
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            onBulkRestore(deletedIds)
                        }
                    }
                }
                onDismissBulkDelete()
            },
            onDismiss = onDismissBulkDelete
        )
    }
}
