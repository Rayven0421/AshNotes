package com.rayven0421.ashnotes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rayven0421.ashnotes.domain.model.NoteSortOption
import com.rayven0421.ashnotes.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val NOTE_SORT_OPTION = stringPreferencesKey("note_sort_option")
    }

    override val noteSortOption: Flow<NoteSortOption> = context.dataStore.data
        .map { preferences ->
            val name = preferences[PreferencesKeys.NOTE_SORT_OPTION] ?: NoteSortOption.UPDATED.name
            try {
                NoteSortOption.valueOf(name)
            } catch (_: IllegalArgumentException) {
                NoteSortOption.UPDATED
            }
        }

    override suspend fun setNoteSortOption(option: NoteSortOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTE_SORT_OPTION] = option.name
        }
    }
}
