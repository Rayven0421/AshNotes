package com.rayven0421.ashnotes.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.domain.model.AuthUser
import com.rayven0421.ashnotes.domain.repository.AuthRepository
import com.rayven0421.ashnotes.domain.repository.SessionRepository
import com.rayven0421.ashnotes.domain.repository.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsContent(
    val currentUser: AuthUser? = null,
    val isLocalMode: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val uiState: StateFlow<UiState<SettingsContent>> = combine(
        authRepository.currentUser,
        sessionRepository.isLocalMode
    ) { user, isLocal ->
        UiState.Success(SettingsContent(currentUser = user, isLocalMode = isLocal))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            sessionRepository.setLocalMode(false)
        }
    }

    fun deleteLocalData(onSuccessMessage: String) {
        viewModelScope.launch {
            workspaceRepository.clearAllLocalData()
            _userMessage.emit(onSuccessMessage)
        }
    }
}
