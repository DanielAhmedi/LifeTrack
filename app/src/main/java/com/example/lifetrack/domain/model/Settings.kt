package com.example.lifetrack.domain.model

data class Settings(
    val onboardingCompleted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val remindersEnabled: Boolean = true
)
