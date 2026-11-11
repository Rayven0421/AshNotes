package com.rayven0421.ashnotes.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayven0421.ashnotes.domain.repository.AuthRepository
import com.rayven0421.ashnotes.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface AuthGateState {
    data object Loading : AuthGateState
    data class Authenticated(val isUserOrLocal: Boolean) : AuthGateState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
    sessionRepository: SessionRepository
) : ViewModel() {

    val authGateState: StateFlow<AuthGateState> = combine(
        authRepository.currentUser,
        sessionRepository.isLocalMode
    ) { user, isLocal ->
        AuthGateState.Authenticated(user != null || isLocal)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthGateState.Loading
    )
}
