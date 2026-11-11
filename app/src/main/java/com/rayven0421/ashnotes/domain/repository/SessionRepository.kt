package com.rayven0421.ashnotes.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val isLocalMode: Flow<Boolean>
    suspend fun setLocalMode(enabled: Boolean)
}
