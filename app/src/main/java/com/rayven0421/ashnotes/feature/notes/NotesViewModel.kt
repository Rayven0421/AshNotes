package com.rayven0421.ashnotes.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.common.formatNoteDate
import com.rayven0421.ashnotes.domain.model.NoteSortOption
import com.rayven0421.ashnotes.domain.repository.NoteRepository
import com.rayven0421.ashnotes.domain.repository.UserPreferencesRepository
import com.rayven0421.ashnotes.domain.repository.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    val sortOption: StateFlow<NoteSortOption> = userPreferencesRepository.noteSortOption.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NoteSortOption.UPDATED
    )

    private val _isArchivedView = MutableStateFlow(false)
    val isArchivedView: StateFlow<Boolean> = _isArchivedView.asStateFlow()

    private val _errorMessageResId = MutableStateFlow<Int?>(null)
    val errorMessageResId: StateFlow<Int?> = _errorMessageResId.asStateFlow()

    val uiState: StateFlow<UiState<NotesContent>> = combine(
        workspaceRepository.observeActiveWorkspaceId(),
        _searchQuery,
        _selectedIds,
        userPreferencesRepository.noteSortOption,
        _isArchivedView
    ) { workspaceId, query, selectedIds, sortOption, isArchived ->
        Quintuple(workspaceId, query, selectedIds, sortOption, isArchived)
    }
        .flatMapLatest { (workspaceId, query, currentSelectedIds, sortOption, isArchived) ->
            noteRepository.observeNotes(workspaceId, query, sortOption, isArchived).map { notes ->
                Quintuple(notes, currentSelectedIds, sortOption, isArchived, query)
            }
        }
        .map { (notes, currentSelectedIds, sortOption, isArchived, query) ->
            if (notes.isEmpty() && query.isEmpty()) {
                if (currentSelectedIds.isNotEmpty()) {
                    _selectedIds.value = emptySet()
                }
                UiState.Empty
            } else {
                val validNoteIds = notes.map { it.id }.toSet()
                val cleanedSelectedIds = currentSelectedIds.intersect(validNoteIds)
                if (cleanedSelectedIds.size != currentSelectedIds.size) {
                    _selectedIds.value = cleanedSelectedIds
                }

                val noteItems = notes.map { note ->
                    NoteItem(
                        id = note.id,
                        title = note.title,
                        preview = note.preview,
                        updatedTime = formatNoteDate(note.updatedAt),
                        isPinned = note.isPinned,
                        isArchived = note.isArchived
                    )
                }
                val pinned = noteItems.filter { it.isPinned }
                val others = noteItems.filter { !it.isPinned }
                UiState.Success(
                    NotesContent(
                        searchQuery = query,
                        pinnedNotes = pinned,
                        otherNotes = others,
                        selectedIds = cleanedSelectedIds,
                        sortOption = sortOption,
                        isArchivedView = isArchived
                    )
                ) as UiState<NotesContent>
            }
        }
        .catch { throwable ->
            emit(UiState.Error(throwable.message ?: "", throwable) as UiState<NotesContent>)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: NoteSortOption) {
        viewModelScope.launch {
            userPreferencesRepository.setNoteSortOption(option)
        }
    }

    fun toggleArchivedView() {
        _isArchivedView.update { !it }
        clearSelection()
    }

    fun setArchivedView(isArchived: Boolean) {
        _isArchivedView.value = isArchived
        clearSelection()
    }

    fun toggleSelectNote(noteId: String) {
        _selectedIds.update { ids ->
            if (noteId in ids) ids - noteId else ids + noteId
        }
    }

    fun selectAllNotes(allIds: List<String>) {
        _selectedIds.value = allIds.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun bulkPinSelected(isPinned: Boolean) {
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            noteRepository.bulkPinNotes(ids, isPinned)
            clearSelection()
        }
    }

    fun bulkArchiveSelected(isArchived: Boolean, onCompleted: ((List<String>) -> Unit)? = null) {
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            noteRepository.bulkArchiveNotes(ids, isArchived)
            clearSelection()
            onCompleted?.invoke(ids)
        }
    }

    fun bulkArchiveNotes(ids: List<String>, isArchived: Boolean) {
        viewModelScope.launch {
            noteRepository.bulkArchiveNotes(ids, isArchived)
        }
    }

    fun bulkDeleteSelected(onDeleted: (List<String>) -> Unit) {
        val ids = _selectedIds.value.toList()
        viewModelScope.launch {
            noteRepository.bulkDeleteNotes(ids)
            clearSelection()
            onDeleted(ids)
        }
    }

    fun bulkRestoreNotes(ids: List<String>) {
        viewModelScope.launch {
            noteRepository.bulkRestoreNotes(ids)
        }
    }

    fun createNewNote(title: String, onNoteCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val activeWorkspaceId = workspaceRepository.observeActiveWorkspaceId().first()
                val id = noteRepository.createNote(workspaceId = activeWorkspaceId, title = title.trim())
                onNoteCreated(id)
            } catch (_: Exception) {
                _errorMessageResId.value = R.string.error_failed_to_create_note
            }
        }
    }

    fun togglePin(noteId: String) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId)
            if (note != null) {
                noteRepository.updateNote(note.copy(isPinned = !note.isPinned))
            }
        }
    }

    fun toggleArchive(noteId: String) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId)
            if (note != null) {
                noteRepository.updateNote(note.copy(isArchived = !note.isArchived))
            }
        }
    }

    fun renameNote(noteId: String, newTitle: String) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId)
            if (note != null && newTitle.isNotBlank()) {
                noteRepository.updateNote(
                    note.copy(
                        title = newTitle.trim(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
        }
    }

    fun restoreNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.restoreNote(noteId)
        }
    }

    fun clearErrorMessage() {
        _errorMessageResId.value = null
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

data class NotesContent(
    val searchQuery: String = "",
    val pinnedNotes: List<NoteItem> = emptyList(),
    val otherNotes: List<NoteItem> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val sortOption: NoteSortOption = NoteSortOption.UPDATED,
    val isArchivedView: Boolean = false
)

data class NoteItem(
    val id: String,
    val title: String,
    val preview: String,
    val updatedTime: String,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
)
