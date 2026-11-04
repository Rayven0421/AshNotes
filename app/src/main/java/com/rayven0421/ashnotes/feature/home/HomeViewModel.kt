package com.rayven0421.ashnotes.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.core.common.formatNoteDate
import com.rayven0421.ashnotes.domain.repository.NoteRepository
import com.rayven0421.ashnotes.domain.repository.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {

    private val _errorMessageResId = MutableStateFlow<Int?>(null)
    val errorMessageResId: StateFlow<Int?> = _errorMessageResId.asStateFlow()

    val uiState: StateFlow<UiState<HomeContent>> = workspaceRepository.observeActiveWorkspaceId()
        .flatMapLatest { workspaceId ->
            noteRepository.observeNotes(workspaceId)
        }
        .map { notes ->
            val recentNotes = notes
                .filter { !it.isArchived }
                .sortedByDescending { it.updatedAt }
                .take(3)
                .map { note ->
                    HomeNoteItem(
                        id = note.id,
                        title = note.title,
                        preview = "",
                        updatedTime = formatNoteDate(note.updatedAt)
                    )
                }

            UiState.Success(
                HomeContent(
                    greetingResId = getGreetingByTimeOfDayResId(),
                    spentMinor = 0L,
                    totalMinor = 0L,
                    paceFraction = 0f,
                    hasBudget = false,
                    notes = recentNotes,
                    activity = emptyList(),
                    syncStatusResId = R.string.home_sync_local_only
                )
            ) as UiState<HomeContent>
        }
        .catch { throwable ->
            emit(UiState.Error(throwable.message ?: "", throwable) as UiState<HomeContent>)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

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

    fun clearErrorMessage() {
        _errorMessageResId.value = null
    }

    private fun getGreetingByTimeOfDayResId(): Int {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> R.string.home_greeting_morning
            in 12..17 -> R.string.home_greeting_afternoon
            else -> R.string.home_greeting_evening
        }
    }
}

data class HomeContent(
    val greetingResId: Int,
    val spentMinor: Long,
    val totalMinor: Long,
    val paceFraction: Float,
    val hasBudget: Boolean,
    val notes: List<HomeNoteItem>,
    val activity: List<HomeActivityItem>,
    val syncStatusResId: Int
)

data class HomeNoteItem(
    val id: String,
    val title: String,
    val preview: String,
    val updatedTime: String
)

data class HomeActivityItem(
    val id: String,
    val description: String,
    val time: String
)
