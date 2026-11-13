package com.rayven0421.ashnotes.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.domain.model.AuthUser
import com.rayven0421.ashnotes.domain.model.CloudBackupError
import com.rayven0421.ashnotes.domain.model.CloudBackupResult
import com.rayven0421.ashnotes.domain.repository.AuthRepository
import com.rayven0421.ashnotes.domain.repository.CloudBackupRepository
import com.rayven0421.ashnotes.domain.repository.SessionRepository
import com.rayven0421.ashnotes.domain.repository.UserPreferencesRepository
import com.rayven0421.ashnotes.domain.repository.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SyncDialogState {
    object None : SyncDialogState
    object ChooseAction : SyncDialogState
    object ConfirmRestore : SyncDialogState
    object InProgress : SyncDialogState
    data class Success(val titleResId: Int, val message: String) : SyncDialogState
    data class Error(val messageResId: Int) : SyncDialogState
}

data class SettingsContent(
    val currentUser: AuthUser? = null,
    val isLocalMode: Boolean = false,
    val isBiometricEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val cloudBackupRepository: CloudBackupRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<UiState<SettingsContent>> = combine(
        authRepository.currentUser,
        sessionRepository.isLocalMode,
        userPreferencesRepository.isBiometricEnabled
    ) { user, isLocal, biometricEnabled ->
        UiState.Success(
            SettingsContent(
                currentUser = user,
                isLocalMode = isLocal,
                isBiometricEnabled = biometricEnabled
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )

    private val _syncDialogState = MutableStateFlow<SyncDialogState>(SyncDialogState.None)
    val syncDialogState: StateFlow<SyncDialogState> = _syncDialogState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun requestChooseAction() {
        _syncDialogState.value = SyncDialogState.ChooseAction
    }

    fun requestRestoreConfirmation() {
        _syncDialogState.value = SyncDialogState.ConfirmRestore
    }

    fun dismissSyncDialog() {
        _syncDialogState.value = SyncDialogState.None
    }

    fun backupToCloud() {
        viewModelScope.launch {
            _syncDialogState.value = SyncDialogState.InProgress
            when (val result = cloudBackupRepository.backup()) {
                is CloudBackupResult.Success -> {
                    val skippedText = if (result.skippedLarge > 0) {
                        " (${result.skippedLarge} large notes skipped)"
                    } else ""
                    val message = "Uploaded ${result.uploaded} items to cloud.$skippedText"
                    _syncDialogState.value = SyncDialogState.Success(
                        titleResId = R.string.dialog_backup_success_title,
                        message = message
                    )
                }
                is CloudBackupResult.Error -> {
                    _syncDialogState.value = SyncDialogState.Error(mapErrorToResId(result.error))
                }
            }
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            _syncDialogState.value = SyncDialogState.InProgress
            when (val result = cloudBackupRepository.restore()) {
                is CloudBackupResult.Success -> {
                    val message = "Added ${result.added} items from cloud."
                    _syncDialogState.value = SyncDialogState.Success(
                        titleResId = R.string.dialog_restore_success_title,
                        message = message
                    )
                }
                is CloudBackupResult.Error -> {
                    _syncDialogState.value = SyncDialogState.Error(mapErrorToResId(result.error))
                }
            }
        }
    }

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

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBiometricEnabled(enabled)
        }
    }

    fun showUserMessage(message: String) {
        viewModelScope.launch {
            _userMessage.emit(message)
        }
    }

    private fun mapErrorToResId(error: CloudBackupError): Int = when (error) {
        CloudBackupError.NETWORK -> R.string.error_network
        CloudBackupError.NOT_SIGNED_IN -> R.string.error_not_signed_in
        CloudBackupError.PERMISSION_DENIED -> R.string.error_permission_denied
        CloudBackupError.UNKNOWN -> R.string.error_unknown
    }
}
