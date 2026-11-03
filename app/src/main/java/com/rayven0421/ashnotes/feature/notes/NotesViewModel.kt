package com.rayven0421.ashnotes.feature.notes

import androidx.lifecycle.ViewModel
import com.rayven0421.ashnotes.core.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<NotesContent>>(UiState.Empty)
    val uiState: StateFlow<UiState<NotesContent>> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        // Future database search filtering
    }
}

data class NotesContent(
    val searchQuery: String = "",
    val pinnedNotes: List<NoteItem> = emptyList(),
    val otherNotes: List<NoteItem> = emptyList()
)

data class NoteItem(
    val id: String,
    val title: String,
    val preview: String,
    val updatedTime: String,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
)
