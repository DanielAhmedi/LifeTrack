package com.example.lifetrack.ui.screens.achievements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lifetrack.R
import com.example.lifetrack.ui.components.PlaceholderScreen

@Composable
fun AchievementsScreen(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    PlaceholderScreen(
        titleRes = R.string.nav_achievements,
        descriptionRes = R.string.achievements_description,
        modifier = modifier,
        onOpenSettings = onOpenSettings
    )
}