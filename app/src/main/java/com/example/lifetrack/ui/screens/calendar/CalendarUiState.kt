package com.example.lifetrack.ui.screens.calendar

import com.example.lifetrack.domain.model.CompletionStatus
import com.example.lifetrack.domain.model.Habit
import java.time.LocalDate
import java.time.YearMonth

enum class CalendarDayState {
    NONE,
    NOT_MARKED,
    PARTIAL,
    COMPLETE,
    SKIPPED
}

data class CalendarDay(
    val date: LocalDate,
    val state: CalendarDayState,
    val done: Int,
    val skipped: Int,
    val expected: Int,
    val isToday: Boolean,
    val isSelected: Boolean,
    val isFuture: Boolean
)

data class CalendarHabitStatus(
    val habit: Habit,
    val status: CompletionStatus?,
    val isScheduled: Boolean
)

data class CalendarUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val month: YearMonth = YearMonth.now(),
    val monthTitle: String = "",
    val weeks: List<List<CalendarDay?>> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val selectedHabitId: Long? = null,
    val selectedDate: LocalDate? = null,
    val dayHabits: List<CalendarHabitStatus> = emptyList(),
    val hasMarksInMonth: Boolean = false,
    val today: LocalDate = LocalDate.now()
) {
    val hasHabits: Boolean get() = habits.isNotEmpty()

    val canMarkSelectedDay: Boolean get() = selectedDate != null && selectedDate == today
}
