package com.rayven0421.ashnotes.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.domain.repository.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SettingsContent>>(UiState.Success(SettingsContent()))
    val uiState: StateFlow<UiState<SettingsContent>> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun deleteLocalData(onSuccessMessage: String) {
        viewModelScope.launch {
            workspaceRepository.clearAllLocalData()
            _userMessage.emit(onSuccessMessage)
        }
    }
}

class SettingsContent
