package com.example.lifetrack.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.lifetrack.data.mapper.SettingsPreferences
import com.example.lifetrack.data.mapper.toSettings
import com.example.lifetrack.domain.model.Settings
import com.example.lifetrack.domain.model.ThemeMode
import com.example.lifetrack.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val settings: Flow<Settings> = dataStore.data.map { preferences ->
        preferences.toSettings()
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferences.KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferences.KEY_THEME_MODE] = themeMode.name
        }
    }

    override suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferences.KEY_REMINDERS_ENABLED] = enabled
        }
    }
}
