package com.example.lifetrack.domain.statistics

import com.example.lifetrack.domain.model.CompletionStatus
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.model.HabitCompletion
import com.example.lifetrack.domain.progress.calculateHabitStreak
import com.example.lifetrack.domain.schedule.creationDate
import com.example.lifetrack.domain.schedule.isScheduledOn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

data class HabitPeriodStatistics(
    val habit: Habit,
    val expected: Int,
    val done: Int,
    val skipped: Int,
    val currentStreak: Int,
    val maxStreak: Int
) {
    val pending: Int get() = (expected - done - skipped).coerceAtLeast(0)
    val hasExpected: Boolean get() = expected > 0
    val percent: Int get() = percentOf(done, expected)
    val percentOrNull: Int? get() = if (hasExpected) percent else null
}

data class StatisticsTotals(
    val expected: Int = 0,
    val done: Int = 0,
    val skipped: Int = 0
) {
    val pending: Int get() = (expected - done - skipped).coerceAtLeast(0)
    val percent: Int? get() = if (expected > 0) percentOf(done, expected) else null
}

data class WeekdayDone(
    val dayOfWeek: DayOfWeek,
    val done: Int
)

data class StatisticsSummary(
    val range: StatisticsRange,
    val totals: StatisticsTotals,
    val rating: List<HabitPeriodStatistics>,
    val bestHabit: HabitPeriodStatistics?,
    val bestMaxStreakHabit: HabitPeriodStatistics?,
    val bestCurrentStreakHabit: HabitPeriodStatistics?,
    val weekdays: List<WeekdayDone>,
    val doneSharePercent: Int,
    val skippedSharePercent: Int
) {
    val hasExplicitCompletions: Boolean get() = totals.done + totals.skipped > 0
}

fun calculateStatistics(
    habits: List<Habit>,
    completions: List<HabitCompletion>,
    period: StatisticsPeriod,
    today: LocalDate,
    zone: ZoneId = ZoneId.systemDefault()
): StatisticsSummary? {
    val activeHabits = habits.filter { it.isActive }
    val createdOnById = activeHabits.associate { it.id to it.creationDate(zone) }
    val eligibleHabits = activeHabits.filter { createdOnById.getValue(it.id) <= today }
    if (eligibleHabits.isEmpty()) return null

    val start = periodStartDate(
        period = period,
        today = today,
        earliestCreatedOn = eligibleHabits.minOf { createdOnById.getValue(it.id) }
    ) ?: today
    val range = StatisticsRange(start = minOf(start, today), end = today)

    val completionsByHabit = completions.groupBy { it.habitId }
    val weekdayCounts = DayOfWeek.entries.associateWith { 0 }.toMutableMap()

    val perHabit = eligibleHabits.map { habit ->
        val createdOn = createdOnById.getValue(habit.id)
        val statuses = completionsByHabit[habit.id].orEmpty().associateBy { it.date }
        var expected = 0
        var done = 0
        var skipped = 0
        var date = maxOf(range.start, createdOn)
        while (!date.isAfter(range.end)) {
            if (habit.isScheduledOn(date, createdOn)) {
                expected += 1
                when (statuses[date.toEpochDay()]?.status) {
                    CompletionStatus.DONE -> {
                        done += 1
                        weekdayCounts[date.dayOfWeek] = weekdayCounts.getValue(date.dayOfWeek) + 1
                    }
                    CompletionStatus.SKIPPED -> skipped += 1
                    null -> Unit
                }
            }
            date = date.plusDays(1)
        }
        val streak = calculateHabitStreak(
            habit = habit,
            completions = statuses.values.toList(),
            today = today,
            zone = zone,
            from = range.start
        )
        HabitPeriodStatistics(
            habit = habit,
            expected = expected,
            done = done,
            skipped = skipped,
            currentStreak = streak.current,
            maxStreak = streak.max
        )
    }

    val totals = StatisticsTotals(
        expected = perHabit.sumOf { it.expected },
        done = perHabit.sumOf { it.done },
        skipped = perHabit.sumOf { it.skipped }
    )
    val explicitTotal = totals.done + totals.skipped

    return StatisticsSummary(
        range = range,
        totals = totals,
        rating = perHabit
            .filter { it.hasExpected }
            .sortedWith(
                compareByDescending<HabitPeriodStatistics> { it.percent }
                    .thenByDescending { it.done }
                    .thenBy { it.habit.name.lowercase() }
            ),
        bestHabit = perHabit
            .filter { it.done > 0 }
            .maxWithOrNull(
                compareBy<HabitPeriodStatistics> { it.done }
                    .thenByDescending { createdOnById.getValue(it.habit.id).toEpochDay() }
            ),
        bestMaxStreakHabit = perHabit.filter { it.maxStreak > 0 }.maxByOrNull { it.maxStreak },
        bestCurrentStreakHabit = perHabit.filter { it.currentStreak > 0 }.maxByOrNull { it.currentStreak },
        weekdays = DayOfWeek.entries.map { day -> WeekdayDone(day, weekdayCounts.getValue(day)) },
        doneSharePercent = percentOf(totals.done, explicitTotal),
        skippedSharePercent = percentOf(totals.skipped, explicitTotal)
    )
}

internal fun percentOf(part: Int, total: Int): Int =
    if (total <= 0 || part <= 0) 0 else ((part.toDouble() * 100.0) / total).roundToInt()
