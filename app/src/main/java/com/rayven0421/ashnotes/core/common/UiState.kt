package com.rayven0421.ashnotes.core.common

import androidx.compose.runtime.Immutable

/**
 * Common UI state hierarchy for screens per ARCHITECTURE.md Section 5.
 * Every screen handles Loading, Empty, Success, and Error states.
 */
@Immutable
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>
}
