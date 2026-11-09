package com.example.lifetrack.ui.screens.home

import androidx.annotation.StringRes
import com.example.lifetrack.R
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.model.HabitCategory

enum class HomeMode(@StringRes val labelRes: Int) {
    TODAY(R.string.home_mode_today),
    ALL(R.string.home_mode_all)
}

data class HabitGroup(
    val category: HabitCategory?,
    val habits: List<Habit>
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val mode: HomeMode = HomeMode.TODAY,
    val groups: List<HabitGroup> = emptyList(),
    val todayPlanned: Int = 0,
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val pausedCount: Int = 0
) {
    val isEmpty: Boolean get() = groups.isEmpty()
}
