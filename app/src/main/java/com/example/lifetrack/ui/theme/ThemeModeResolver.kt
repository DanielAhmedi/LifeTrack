package com.example.lifetrack.ui.theme

import com.example.lifetrack.domain.model.ThemeMode

fun ThemeMode.resolveDarkTheme(systemInDarkTheme: Boolean): Boolean = when (this) {
    ThemeMode.SYSTEM -> systemInDarkTheme
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}