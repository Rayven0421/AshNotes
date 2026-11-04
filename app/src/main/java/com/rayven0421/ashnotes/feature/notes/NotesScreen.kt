package com.rayven0421.ashnotes.feature.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.core.designsystem.ScreenHeader
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
    onBulkArchiveSelected: (Boolean) -> Unit = {},
    onBulkDeleteSelected: ((List<String>) -> Unit) -> Unit = {},
    onBulkRestoreNotes: (List<String>) -> Unit = {}
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showNewNoteDialog by rememberSaveable { mutableStateOf(false) }
    var noteIdToRename by rememberSaveable { mutableStateOf<String?>(null) }
    var noteIdToDelete by rememberSaveable { mutableStateOf<String?>(null) }
    var showBulkDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val isSelectionMode = selectedIds.isNotEmpty()

    BackHandler(enabled = isSelectionMode) {
        onClearSelection()
    }

    DisposableEffect(Unit) {
        onDispose {
            onClearSelection()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!isSelectionMode && uiState is UiState.Success) {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AshNotesTheme.spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(AshNotesTheme.spacing.medium)
        ) {
            ScreenHeader(title = stringResource(R.string.notes_screen_title))

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
                    onBulkArchive = { onBulkArchiveSelected(true) },
                    onBulkDelete = { showBulkDeleteDialog = true }
                )
            } else {
                NotesSearchRow(
                    searchQuery = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        onSearchQueryChanged(it)
                    },
                    currentSortOption = sortOption,
                    onSortOptionSelected = onSortOptionSelected
                )
            }

            Spacer(modifier = Modifier.height(AshNotesTheme.spacing.extraSmall))

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
                modifier = Modifier.weight(1f)
            )
        }
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
