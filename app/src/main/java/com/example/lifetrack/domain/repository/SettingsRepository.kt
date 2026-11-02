package com.example.lifetrack.domain.repository

import com.example.lifetrack.domain.model.Settings
import com.example.lifetrack.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val settings: Flow<Settings>

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setRemindersEnabled(enabled: Boolean)
}
