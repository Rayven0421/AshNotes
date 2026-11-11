package com.rayven0421.ashnotes.feature.authentication

import com.rayven0421.ashnotes.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSessionRepository : SessionRepository {
    private val _isLocalMode = MutableStateFlow(false)
    override val isLocalMode: StateFlow<Boolean> = _isLocalMode.asStateFlow()

    override suspend fun setLocalMode(enabled: Boolean) {
        _isLocalMode.value = enabled
    }
}
