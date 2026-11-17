package com.example.lifetrack.domain.progress

import com.example.lifetrack.domain.model.CompletionStatus
import com.example.lifetrack.domain.model.FrequencyType
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.model.HabitCompletion
import com.example.lifetrack.domain.schedule.weekDayBit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class HabitStreakTest {

    private val zone: ZoneId = ZoneId.of("UTC")
    private val createdOn: LocalDate = LocalDate.of(2026, 10, 1)
    private val mondayToFridayMask =
        weekDayBit(DayOfWeek.MONDAY) or
            weekDayBit(DayOfWeek.WEDNESDAY) or
            weekDayBit(DayOfWeek.FRIDAY)

    private fun dailyHabit(isActive: Boolean = true): Habit = Habit(
        id = 1L,
        name = "Привычка",
        icon = "star",
        color = 0xFF3D9A50.toInt(),
        createdAt = createdOn.atStartOfDay(zone).toInstant().toEpochMilli(),
        frequencyType = FrequencyType.DAILY,
        isActive = isActive
    )

    private fun weeklyHabit(isActive: Boolean = true): Habit = Habit(
        id = 2L,
        name = "Привычка по дням",
        icon = "star",
        color = 0xFF3D9A50.toInt(),
        createdAt = createdOn.atStartOfDay(zone).toInstant().toEpochMilli(),
        frequencyType = FrequencyType.WEEKLY,
        weekDaysMask = mondayToFridayMask,
        isActive = isActive
    )

    private fun done(habitId: Long, date: String): HabitCompletion = HabitCompletion(
        habitId = habitId,
        date = LocalDate.parse(date).toEpochDay(),
        status = CompletionStatus.DONE
    )

    private fun skipped(habitId: Long, date: String): HabitCompletion = HabitCompletion(
        habitId = habitId,
        date = LocalDate.parse(date).toEpochDay(),
        status = CompletionStatus.SKIPPED
    )

    @Test
    fun skippedDayBreaksCurrentStreakButKeepsMax() {
        val habit = dailyHabit()
        val completions = listOf(
            done(habit.id, "2026-10-20"),
            done(habit.id, "2026-10-21"),
            done(habit.id, "2026-10-22"),
            skipped(habit.id, "2026-10-23"),
            done(habit.id, "2026-10-24"),
            done(habit.id, "2026-10-25")
        )

        val streak = calculateHabitStreak(habit, completions, LocalDate.parse("2026-10-25"), zone)

        assertEquals(2, streak.current)
        assertEquals(3, streak.max)
    }

    @Test
    fun unmarkedTodayDoesNotBreakCurrentStreak() {
        val habit = dailyHabit()
        val completions = listOf(
            done(habit.id, "2026-10-24"),
            done(habit.id, "2026-10-25")
        )

        val streak = calculateHabitStreak(habit, completions, LocalDate.parse("2026-10-26"), zone)

        assertEquals(2, streak.current)
        assertEquals(2, streak.max)
    }

    @Test
    fun markedTodayExtendsCurrentStreak() {
        val habit = dailyHabit()
        val completions = listOf(
            done(habit.id, "2026-10-24"),
            done(habit.id, "2026-10-25"),
            done(habit.id, "2026-10-26")
        )

        val streak = calculateHabitStreak(habit, completions, LocalDate.parse("2026-10-26"), zone)

        assertEquals(3, streak.current)
        assertEquals(3, streak.max)
    }

    @Test
    fun unmarkedPastScheduledDayResetsCurrentStreak() {
        val habit = dailyHabit()
        val completions = listOf(
            done(habit.id, "2026-10-20"),
            done(habit.id, "2026-10-21"),
            done(habit.id, "2026-10-22")
        )

        val streak = calculateHabitStreak(habit, completions, LocalDate.parse("2026-10-24"), zone)

        assertEquals(0, streak.current)
        assertEquals(3, streak.max)
    }

    @Test
    fun skippedTodayResetsCurrentStreakImmediately() {
        val habit = dailyHabit()
        val completions = listOf(
            done(habit.id, "2026-10-24"),
            done(habit.id, "2026-10-25"),
            skipped(habit.id, "2026-10-26")
        )

        val streak = calculateHabitStreak(habit, completions, LocalDate.parse("2026-10-26"), zone)

        assertEquals(0, streak.current)
        assertEquals(2, streak.max)
    }

    @Test
    fun weeklyHabitIgnoresUnscheduledDays() {
        val habit = weeklyHabit()
        val completions = listOf(
            done(habit.id, "2026-10-19"),
            done(habit.id, "2026-10-21"),
            done(habit.id, "2026-10-23")
        )

        val friday = calculateHabitStreak(habit, completions, LocalDate.parse("2026-10-23"), zone)
        val sunday = calculateHabitStreak(habit, completions, LocalDate.parse("2026-10-25"), zone)

        assertEquals(3, friday.current)
        assertEquals(3, sunday.current)
        assertEquals(3, friday.max)
    }

    @Test
    fun weeklyHabitExtendsStreakOnNextScheduledDay() {
        val habit = weeklyHabit()
        val completions = listOf(
            done(habit.id, "2026-10-19"),
            done(habit.id, "2026-10-21"),
            done(habit.id, "2026-10-23"),
            done(habit.id, "2026-10-26")
        )

        val monday = calculateHabitStreak(habit, completions, LocalDate.parse("2026-10-26"), zone)
        val tuesday = calculateHabitStreak(habit, completions, LocalDate.parse("2026-10-27"), zone)

        assertEquals(4, monday.current)
        assertEquals(4, tuesday.current)
        assertEquals(4, monday.max)
    }

    @Test
    fun missedScheduledDayEndsStreakButMaxIsKept() {
        val habit = weeklyHabit()
        val completions = listOf(
            done(habit.id, "2026-10-19"),
            done(habit.id, "2026-10-21"),
            done(habit.id, "2026-10-23"),
            done(habit.id, "2026-10-26")
        )

        val streak = calculateHabitStreak(habit, completions, LocalDate.parse("2026-10-29"), zone)

        assertEquals(0, streak.current)
        assertEquals(4, streak.max)
    }

    @Test
    fun inactiveHabitHasZeroCurrentStreak() {
        val habit = weeklyHabit(isActive = false)
        val completions = listOf(
            done(habit.id, "2026-10-19"),
            done(habit.id, "2026-10-21"),
            done(habit.id, "2026-10-23"),
            done(habit.id, "2026-10-26")
        )

        val streak = calculateHabitStreak(habit, completions, LocalDate.parse("2026-10-26"), zone)

        assertEquals(0, streak.current)
        assertEquals(4, streak.max)
    }

    @Test
    fun completionsBeforeCreationAndInFutureAreIgnored() {
        val habit = dailyHabit()
        val completions = listOf(
            done(habit.id, "2026-09-30"),
            done(habit.id, "2026-10-01"),
            done(habit.id, "2026-10-02"),
            done(habit.id, "2026-10-03")
        )

        val streak = calculateHabitStreak(habit, completions, LocalDate.parse("2026-10-02"), zone)

        assertEquals(2, streak.current)
        assertEquals(2, streak.max)
    }

    @Test
    fun habitCreatedTodayWithoutMarksHasZeroStreak() {
        val habit = dailyHabit()

        val streak = calculateHabitStreak(habit, emptyList(), createdOn, zone)

        assertEquals(0, streak.current)
        assertEquals(0, streak.max)
    }
}
