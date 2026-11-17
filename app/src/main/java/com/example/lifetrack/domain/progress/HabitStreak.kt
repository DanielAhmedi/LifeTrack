package com.example.lifetrack.domain.progress

import com.example.lifetrack.domain.model.CompletionStatus
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.model.HabitCompletion
import com.example.lifetrack.domain.schedule.creationDate
import com.example.lifetrack.domain.schedule.isScheduledOn
import java.time.LocalDate
import java.time.ZoneId

data class HabitStreak(
    val current: Int = 0,
    val max: Int = 0
)

fun calculateHabitStreak(
    habit: Habit,
    completions: List<HabitCompletion>,
    today: LocalDate,
    zone: ZoneId = ZoneId.systemDefault()
): HabitStreak {
    val createdOn = habit.creationDate(zone)
    if (today.isBefore(createdOn)) return HabitStreak()

    val statuses = completions
        .filter { it.date <= today.toEpochDay() }
        .associate { LocalDate.ofEpochDay(it.date) to it.status }

    val maxStreak = calculateMaxStreak(habit, statuses, createdOn, today)
    val currentStreak =
        if (habit.isActive) calculateCurrentStreak(habit, statuses, createdOn, today) else 0
    return HabitStreak(current = currentStreak, max = maxStreak)
}

private fun calculateCurrentStreak(
    habit: Habit,
    statuses: Map<LocalDate, CompletionStatus>,
    createdOn: LocalDate,
    today: LocalDate
): Int {
    var streak = 0
    var date = today
    while (!date.isBefore(createdOn)) {
        if (habit.isScheduledOn(date, createdOn)) {
            when (statuses[date]) {
                CompletionStatus.DONE -> streak += 1
                CompletionStatus.SKIPPED -> return streak
                null -> if (date != today) return streak
            }
        }
        date = date.minusDays(1)
    }
    return streak
}

private fun calculateMaxStreak(
    habit: Habit,
    statuses: Map<LocalDate, CompletionStatus>,
    createdOn: LocalDate,
    today: LocalDate
): Int {
    var maxStreak = 0
    var streak = 0
    var date = createdOn
    while (!date.isAfter(today)) {
        if (habit.isScheduledOn(date, createdOn)) {
            if (statuses[date] == CompletionStatus.DONE) {
                streak += 1
                if (streak > maxStreak) maxStreak = streak
            } else {
                streak = 0
            }
        }
        date = date.plusDays(1)
    }
    return maxStreak
}
