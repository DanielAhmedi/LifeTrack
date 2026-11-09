package com.example.lifetrack.ui.screens.statistics

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lifetrack.R
import com.example.lifetrack.ui.components.PlaceholderScreen

@Composable
fun StatisticsScreen(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    PlaceholderScreen(
        titleRes = R.string.nav_statistics,
        descriptionRes = R.string.statistics_description,
        modifier = modifier,
        onOpenSettings = onOpenSettings
    )
}