package com.example.lifetrack.ui.screens.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lifetrack.R
import com.example.lifetrack.ui.components.PlaceholderScreen

@Composable
fun CalendarScreen(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    PlaceholderScreen(
        titleRes = R.string.nav_calendar,
        descriptionRes = R.string.calendar_description,
        modifier = modifier,
        onOpenSettings = onOpenSettings
    )
}