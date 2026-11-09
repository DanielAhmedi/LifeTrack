package com.example.lifetrack.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lifetrack.R
import com.example.lifetrack.ui.components.PlaceholderScreen

@Composable
fun HomeScreen(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    PlaceholderScreen(
        titleRes = R.string.nav_home,
        descriptionRes = R.string.home_description,
        modifier = modifier,
        onOpenSettings = onOpenSettings
    )
}