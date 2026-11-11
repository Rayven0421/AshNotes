package com.rayven0421.ashnotes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.rayven0421.ashnotes.domain.repository.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_settings")

@Singleton
class SessionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SessionRepository {

    private object PreferencesKeys {
        val IS_LOCAL_MODE = booleanPreferencesKey("is_local_mode")
    }

    override val isLocalMode: Flow<Boolean> = context.sessionDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_LOCAL_MODE] ?: false
        }

    override suspend fun setLocalMode(enabled: Boolean) {
        context.sessionDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOCAL_MODE] = enabled
        }
    }
}
