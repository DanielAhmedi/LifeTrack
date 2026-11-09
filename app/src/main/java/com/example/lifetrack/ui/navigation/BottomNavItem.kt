package com.example.lifetrack.ui.navigation

import com.example.lifetrack.R

enum class BottomNavItem(val route: String, val labelRes: Int) {
    HOME(Routes.HOME, R.string.nav_home),
    CALENDAR(Routes.CALENDAR, R.string.nav_calendar),
    STATISTICS(Routes.STATISTICS, R.string.nav_statistics),
    ACHIEVEMENTS(Routes.ACHIEVEMENTS, R.string.nav_achievements);

    companion object {
        fun fromRoute(route: String?): BottomNavItem? = entries.firstOrNull { it.route == route }
    }
}