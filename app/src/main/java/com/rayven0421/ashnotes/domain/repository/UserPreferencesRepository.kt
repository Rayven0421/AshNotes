package com.rayven0421.ashnotes.domain.repository

import com.rayven0421.ashnotes.domain.model.NoteSortOption
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val noteSortOption: Flow<NoteSortOption>
    suspend fun setNoteSortOption(option: NoteSortOption)

    val isBiometricEnabled: Flow<Boolean>
    suspend fun setBiometricEnabled(enabled: Boolean)
}
