package com.rayven0421.ashnotes.feature.home

import androidx.lifecycle.ViewModel
import com.rayven0421.ashnotes.core.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<HomeContent>>(UiState.Empty)
    val uiState: StateFlow<UiState<HomeContent>> = _uiState.asStateFlow()
}

data class HomeContent(
    val greeting: String,
    val spentMinor: Long,
    val totalMinor: Long,
    val paceFraction: Float,
    val hasBudget: Boolean,
    val notes: List<HomeNoteItem>,
    val activity: List<HomeActivityItem>,
    val syncStatus: String
)
