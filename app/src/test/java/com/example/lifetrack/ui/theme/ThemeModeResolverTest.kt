package com.example.lifetrack.ui.theme

import com.example.lifetrack.domain.model.ThemeMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeResolverTest {

    @Test
    fun systemThemeFollowsDeviceSetting() {
        assertTrue(ThemeMode.SYSTEM.resolveDarkTheme(systemInDarkTheme = true))
        assertFalse(ThemeMode.SYSTEM.resolveDarkTheme(systemInDarkTheme = false))
    }

    @Test
    fun lightThemeIsAlwaysLight() {
        assertFalse(ThemeMode.LIGHT.resolveDarkTheme(systemInDarkTheme = true))
        assertFalse(ThemeMode.LIGHT.resolveDarkTheme(systemInDarkTheme = false))
    }

    @Test
    fun darkThemeIsAlwaysDark() {
        assertTrue(ThemeMode.DARK.resolveDarkTheme(systemInDarkTheme = true))
        assertTrue(ThemeMode.DARK.resolveDarkTheme(systemInDarkTheme = false))
    }
}