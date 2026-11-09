package com.example.lifetrack.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lifetrack.R
import com.example.lifetrack.ui.components.PlaceholderScreen

@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    PlaceholderScreen(
        titleRes = R.string.settings_title,
        descriptionRes = R.string.settings_description,
        modifier = modifier,
        onBack = onBack
    )
}