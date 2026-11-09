package com.example.lifetrack.ui.navigation

object Routes {

    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val STATISTICS = "statistics"
    const val ACHIEVEMENTS = "achievements"
    const val SETTINGS = "settings"

    const val HABIT_EDITOR = "habit_editor"
    const val HABIT_ID_ARG = "habitId"
    const val NO_HABIT_ID = -1L
    const val HABIT_EDITOR_ROUTE = "$HABIT_EDITOR?$HABIT_ID_ARG={$HABIT_ID_ARG}"
    const val HABIT_MESSAGE_KEY = "habit_message"

    fun habitEditorRoute(habitId: Long? = null): String =
        if (habitId == null || habitId <= 0L) {
            HABIT_EDITOR
        } else {
            "$HABIT_EDITOR?$HABIT_ID_ARG=$habitId"
        }
}
