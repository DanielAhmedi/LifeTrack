package com.example.lifetrack.data.mapper

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.lifetrack.domain.model.Settings
import com.example.lifetrack.domain.model.ThemeMode

object SettingsPreferences {

    val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    val KEY_REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")

    const val DEFAULT_ONBOARDING_COMPLETED = false
    const val DEFAULT_REMINDERS_ENABLED = true
    val DEFAULT_THEME_MODE = ThemeMode.SYSTEM
}

fun Preferences.toSettings(): Settings = Settings(
    onboardingCompleted = this[SettingsPreferences.KEY_ONBOARDING_COMPLETED]
        ?: SettingsPreferences.DEFAULT_ONBOARDING_COMPLETED,
    themeMode = themeModeFromStorage(this[SettingsPreferences.KEY_THEME_MODE]),
    remindersEnabled = this[SettingsPreferences.KEY_REMINDERS_ENABLED]
        ?: SettingsPreferences.DEFAULT_REMINDERS_ENABLED
)

fun themeModeFromStorage(value: String?): ThemeMode =
    ThemeMode.entries.firstOrNull { it.name == value } ?: SettingsPreferences.DEFAULT_THEME_MODE
