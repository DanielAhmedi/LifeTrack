package com.example.lifetrack.data.mapper

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.example.lifetrack.domain.model.Settings
import com.example.lifetrack.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsMappersTest {

    @Test
    fun defaultSettingsMatchDocumentedValues() {
        val settings = Settings()

        assertEquals(false, settings.onboardingCompleted)
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertEquals(true, settings.remindersEnabled)
    }

    @Test
    fun emptyPreferencesProduceDefaultSettings() {
        assertEquals(Settings(), emptyPreferences().toSettings())
    }

    @Test
    fun storedPreferencesAreRead() {
        val preferences = mutablePreferencesOf(
            SettingsPreferences.KEY_ONBOARDING_COMPLETED to true,
            SettingsPreferences.KEY_THEME_MODE to "DARK",
            SettingsPreferences.KEY_REMINDERS_ENABLED to false
        )

        assertEquals(
            Settings(
                onboardingCompleted = true,
                themeMode = ThemeMode.DARK,
                remindersEnabled = false
            ),
            preferences.toSettings()
        )
    }

    @Test
    fun themeModeIsParsedFromStorageValue() {
        assertEquals(ThemeMode.LIGHT, themeModeFromStorage("LIGHT"))
        assertEquals(ThemeMode.SYSTEM, themeModeFromStorage("SYSTEM"))
        assertEquals(ThemeMode.SYSTEM, themeModeFromStorage(null))
        assertEquals(ThemeMode.SYSTEM, themeModeFromStorage("UNKNOWN"))
    }
}
