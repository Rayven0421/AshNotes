package com.rayven0421.ashnotes.feature.settings

import androidx.lifecycle.ViewModel
import com.rayven0421.ashnotes.core.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<SettingsContent>>(UiState.Success(SettingsContent()))
    val uiState: StateFlow<UiState<SettingsContent>> = _uiState.asStateFlow()
}

class SettingsContent
