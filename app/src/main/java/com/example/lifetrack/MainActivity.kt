package com.example.lifetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.lifetrack.domain.model.Settings
import com.example.lifetrack.ui.navigation.LifeTrackApp
import com.example.lifetrack.ui.theme.LifeTrackTheme
import com.example.lifetrack.ui.theme.resolveDarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsRepository =
            (application as LifeTrackApplication).container.settingsRepository
        setContent {
            val settings by settingsRepository.settings.collectAsState(initial = Settings())
            LifeTrackTheme(darkTheme = settings.themeMode.resolveDarkTheme(isSystemInDarkTheme())) {
                LifeTrackApp()
            }
        }
    }
}