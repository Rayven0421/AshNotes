package com.rayven0421.ashnotes.feature.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.TabScreenLayout
import com.rayven0421.ashnotes.domain.model.NoteSortOption
import com.rayven0421.ashnotes.feature.notes.components.NotesContentArea
import com.rayven0421.ashnotes.feature.notes.components.NotesDialogs
import com.rayven0421.ashnotes.feature.notes.components.NotesSearchRow
import com.rayven0421.ashnotes.feature.notes.components.SelectionTopBar
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    uiState: UiState<NotesContent> = UiState.Empty,
    sortOption: NoteSortOption = NoteSortOption.UPDATED,
    onSortOptionSelected: (NoteSortOption) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onToggleArchivedView: () -> Unit = {},
    onNoteClick: (String) -> Unit = {},
    onNewNoteClick: (String) -> Unit = {},
    onTogglePin: (String) -> Unit = {},
    onToggleArchive: (String) -> Unit = {},
    onRename: (String, String) -> Unit = { _, _ -> },
    onDelete: (String) -> Unit = {},
    onRestore: (String) -> Unit = {},
    selectedIds: Set<String> = emptySet(),
    onToggleSelectNote: (String) -> Unit = {},
    onSelectAllNotes: (List<String>) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onBulkPinSelected: (Boolean) -> Unit = {},
    onBulkArchiveSelected: (Boolean, ((List<String>) -> Unit)?) -> Unit = { _, _ -> },
    onBulkArchiveNotes: (List<String>, Boolean) -> Unit = { _, _ -> },
    onBulkDeleteSelected: ((List<String>) -> Unit) -> Unit = {},
    onBulkRestoreNotes: (List<String>) -> Unit = {},
    isArchivedView: Boolean = false
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showNewNoteDialog by rememberSaveable { mutableStateOf(false) }
    var noteIdToRename by rememberSaveable { mutableStateOf<String?>(null) }
    var noteIdToDelete by rememberSaveable { mutableStateOf<String?>(null) }
    var showBulkDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val isSelectionMode = selectedIds.isNotEmpty()
    val undoLabel = stringResource(R.string.action_undo)

    BackHandler(enabled = isSelectionMode) {
        onClearSelection()
    }

    DisposableEffect(Unit) {
        onDispose {
            onClearSelection()
        }
    }

    TabScreenLayout(
        title = stringResource(if (isArchivedView) R.string.notes_archived_title else R.string.notes_screen_title),
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            if (!isSelectionMode && !isArchivedView && uiState is UiState.Success) {
                FloatingActionButton(
                    onClick = { showNewNoteDialog = true },
                    containerColor = AshNotesTheme.colors.ember,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.notes_new_note)
                    )
                }
            }
        }
    ) {
        if (isSelectionMode) {
            val totalCount = if (uiState is UiState.Success) {
                uiState.data.pinnedNotes.size + uiState.data.otherNotes.size
            } else {
                0
            }
            SelectionTopBar(
                selectedCount = selectedIds.size,
                totalCount = totalCount,
                onCloseSelection = onClearSelection,
                onSelectAllToggle = {
                    if (uiState is UiState.Success) {
                        val allIds = (uiState.data.pinnedNotes + uiState.data.otherNotes).map { it.id }
                        if (selectedIds.size == allIds.size) {
                            onClearSelection()
                        } else {
                            onSelectAllNotes(allIds)
                        }
                    }
                },
                onBulkPin = { onBulkPinSelected(true) },
                onBulkArchive = {
                    val targetArchive = !isArchivedView
                    onBulkArchiveSelected(targetArchive) { ids ->
                        scope.launch {
                            val message = if (targetArchive) {
                                context.resources.getQuantityString(R.plurals.snackbar_bulk_archived, ids.size, ids.size)
                            } else {
                                context.resources.getQuantityString(R.plurals.snackbar_bulk_unarchived, ids.size, ids.size)
                            }
                            val result = snackbarHostState.showSnackbar(
                                message = message,
                                actionLabel = undoLabel
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onBulkArchiveNotes(ids, !targetArchive)
                            }
                        }
                    }
                },
                onBulkDelete = { showBulkDeleteDialog = true },
                isArchivedView = isArchivedView
            )
        } else {
            NotesSearchRow(
                searchQuery = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    onSearchQueryChanged(it)
                },
                currentSortOption = sortOption,
                onSortOptionSelected = onSortOptionSelected,
                isArchivedView = isArchivedView,
                onToggleArchivedView = onToggleArchivedView
            )

            if (isArchivedView) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AshNotesTheme.colors.emberSoft, MaterialTheme.shapes.small)
                        .padding(horizontal = AshNotesTheme.spacing.medium, vertical = AshNotesTheme.spacing.extraSmall),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = AshNotesTheme.colors.ember,
                            modifier = Modifier.size(AshNotesTheme.spacing.default)
                        )
                        Text(
                            text = stringResource(R.string.notes_view_archived),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AshNotesTheme.colors.ember
                        )
                    }
                    TextButton(onClick = onToggleArchivedView) {
                        Text(
                            text = stringResource(R.string.action_exit_archived),
                            style = MaterialTheme.typography.labelMedium,
                            color = AshNotesTheme.colors.ember
                        )
                    }
                }
            }
        }

        NotesContentArea(
            uiState = uiState,
            selectedIds = selectedIds,
            isSelectionMode = isSelectionMode,
            onNoteClick = onNoteClick,
            onNoteLongClick = { id -> onToggleSelectNote(id) },
            onToggleSelect = { id -> onToggleSelectNote(id) },
            onNewNoteClick = { showNewNoteDialog = true },
            onTogglePin = onTogglePin,
            onToggleArchive = onToggleArchive,
            onRenameRequest = { note -> noteIdToRename = note.id },
            onDeleteRequest = { note -> noteIdToDelete = note.id },
            isArchivedView = isArchivedView,
            modifier = Modifier.weight(1f)
        )
    }

    NotesDialogs(
        uiState = uiState,
        showNewNoteDialog = showNewNoteDialog,
        onDismissNewNote = { showNewNoteDialog = false },
        onConfirmNewNote = onNewNoteClick,
        noteIdToRename = noteIdToRename,
        onDismissRename = { noteIdToRename = null },
        onConfirmRename = onRename,
        noteIdToDelete = noteIdToDelete,
        onDismissDelete = { noteIdToDelete = null },
        onConfirmDelete = onDelete,
        showBulkDeleteDialog = showBulkDeleteDialog,
        bulkDeleteCount = selectedIds.size,
        onDismissBulkDelete = { showBulkDeleteDialog = false },
        onConfirmBulkDelete = { onDeletedCallback ->
            onBulkDeleteSelected(onDeletedCallback)
        },
        onRestore = onRestore,
        onBulkRestore = onBulkRestoreNotes,
        snackbarHostState = snackbarHostState,
        scope = scope
    )
}
