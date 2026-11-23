package com.example.lifetrack.domain.statistics

import com.example.lifetrack.domain.model.CompletionStatus
import com.example.lifetrack.domain.model.FrequencyType
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.model.HabitCompletion
import com.example.lifetrack.domain.schedule.weekDayBit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticsCalculatorTest {

    private val zone: ZoneId = ZoneId.of("UTC")
    private val today: LocalDate = LocalDate.of(2026, 10, 29)
    private val weekStart: LocalDate = LocalDate.of(2026, 10, 26)
    private val monthStart: LocalDate = LocalDate.of(2026, 10, 1)
    private val mondayWednesdayFridayMask =
        weekDayBit(DayOfWeek.MONDAY) or
            weekDayBit(DayOfWeek.WEDNESDAY) or
            weekDayBit(DayOfWeek.FRIDAY)

    private fun habit(
        id: Long,
        name: String = "Привычка $id",
        frequencyType: FrequencyType = FrequencyType.DAILY,
        weekDaysMask: Int? = null,
        intervalDays: Int? = null,
        isActive: Boolean = true,
        createdOn: LocalDate = monthStart
    ): Habit = Habit(
        id = id,
        name = name,
        icon = "star",
        color = 0xFF3D9A50.toInt(),
        createdAt = createdOn.atStartOfDay(zone).toInstant().toEpochMilli(),
        frequencyType = frequencyType,
        weekDaysMask = weekDaysMask,
        intervalDays = intervalDays,
        isActive = isActive
    )

    private fun done(habitId: Long, date: LocalDate): HabitCompletion = HabitCompletion(
        habitId = habitId,
        date = date.toEpochDay(),
        status = CompletionStatus.DONE
    )

    private fun skipped(habitId: Long, date: LocalDate): HabitCompletion = HabitCompletion(
        habitId = habitId,
        date = date.toEpochDay(),
        status = CompletionStatus.SKIPPED
    )

    private fun calculate(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        period: StatisticsPeriod
    ): StatisticsSummary? = calculateStatistics(habits, completions, period, today, zone)

    @Test
    fun weekRangeStartsOnMondayAndEndsToday() {
        val summary = calculate(listOf(habit(1)), emptyList(), StatisticsPeriod.WEEK)!!

        assertEquals(weekStart, summary.range.start)
        assertEquals(today, summary.range.end)
        assertEquals(4, summary.totals.expected)
    }

    @Test
    fun monthRangeStartsOnFirstDayAndEndsToday() {
        val summary = calculate(listOf(habit(1)), emptyList(), StatisticsPeriod.MONTH)!!

        assertEquals(monthStart, summary.range.start)
        assertEquals(today, summary.range.end)
        assertEquals(29, summary.totals.expected)
    }

    @Test
    fun allTimeRangeStartsAtEarliestActiveHabitCreation() {
        val habits = listOf(
            habit(1),
            habit(2, createdOn = LocalDate.of(2026, 10, 15))
        )

        val summary = calculate(habits, emptyList(), StatisticsPeriod.ALL)!!

        assertEquals(monthStart, summary.range.start)
        assertEquals(today, summary.range.end)
        assertEquals(44, summary.totals.expected)
        assertEquals(15, summary.rating.last().expected)
    }

    @Test
    fun intervalHabitHonoursCreationDateOffsets() {
        val summary = calculate(
            habits = listOf(habit(1, frequencyType = FrequencyType.INTERVAL, intervalDays = 3)),
            completions = emptyList(),
            period = StatisticsPeriod.MONTH
        )!!

        assertEquals(10, summary.totals.expected)
    }


    @Test
    fun onlyScheduledDaysAreCountedAsExpected() {
        val habit = habit(
            id = 1,
            frequencyType = FrequencyType.WEEKLY,
            weekDaysMask = mondayWednesdayFridayMask
        )
        val completions = listOf(
            done(habit.id, LocalDate.of(2026, 10, 26)),
            done(habit.id, LocalDate.of(2026, 10, 27))
        )

        val summary = calculate(listOf(habit), completions, StatisticsPeriod.WEEK)!!

        assertEquals(2, summary.totals.expected)
        assertEquals(1, summary.totals.done)
        assertEquals(1, summary.totals.pending)
        assertEquals(50, summary.totals.percent)
    }

    @Test
    fun skippedAndUnmarkedDaysAreNotDone() {
        val habit = habit(1)
        val completions = listOf(
            done(habit.id, LocalDate.of(2026, 10, 26)),
            done(habit.id, LocalDate.of(2026, 10, 27)),
            skipped(habit.id, LocalDate.of(2026, 10, 28))
        )

        val summary = calculate(listOf(habit), completions, StatisticsPeriod.WEEK)!!

        assertEquals(4, summary.totals.expected)
        assertEquals(2, summary.totals.done)
        assertEquals(1, summary.totals.skipped)
        assertEquals(1, summary.totals.pending)
        assertEquals(50, summary.totals.percent)
    }

    @Test
    fun futureCompletionsAreIgnored() {
        val habit = habit(1)
        val completions = listOf(done(habit.id, today.plusDays(1)))

        val summary = calculate(listOf(habit), completions, StatisticsPeriod.WEEK)!!

        assertEquals(0, summary.totals.done)
        assertEquals(0, summary.totals.percent)
    }

    @Test
    fun percentIsNullWhenNothingWasExpected() {
        val habit = habit(
            id = 1,
            frequencyType = FrequencyType.WEEKLY,
            weekDaysMask = weekDayBit(DayOfWeek.MONDAY),
            createdOn = today
        )

        val summary = calculate(listOf(habit), emptyList(), StatisticsPeriod.WEEK)!!

        assertEquals(0, summary.totals.expected)
        assertNull(summary.totals.percent)
        assertTrue(summary.rating.isEmpty())
        assertEquals(0, summary.weekdays.sumOf { it.done })
    }

    @Test
    fun weekdayCountsOnlyDoneDays() {
        val first = habit(1)
        val second = habit(2)
        val completions = listOf(
            done(first.id, LocalDate.of(2026, 10, 26)),
            done(second.id, LocalDate.of(2026, 10, 26)),
            done(first.id, LocalDate.of(2026, 10, 28)),
            skipped(second.id, LocalDate.of(2026, 10, 29))
        )

        val summary = calculate(listOf(first, second), completions, StatisticsPeriod.WEEK)!!

        assertEquals(2, summary.weekdays.first { it.dayOfWeek == DayOfWeek.MONDAY }.done)
        assertEquals(1, summary.weekdays.first { it.dayOfWeek == DayOfWeek.WEDNESDAY }.done)
        assertEquals(0, summary.weekdays.first { it.dayOfWeek == DayOfWeek.THURSDAY }.done)
        assertEquals(7, summary.weekdays.size)
        assertEquals(3, summary.weekdays.sumOf { it.done })
    }

    @Test
    fun ratingSortsByPercentThenDoneThenName() {
        val most = habit(1, name = "Бег")
        val less = habit(2, name = "Чтение")
        val rare = habit(3, name = "Вода")
        val completions = listOf(
            done(most.id, LocalDate.of(2026, 10, 26)),
            done(most.id, LocalDate.of(2026, 10, 27)),
            done(most.id, LocalDate.of(2026, 10, 28)),
            done(less.id, LocalDate.of(2026, 10, 26)),
            done(less.id, LocalDate.of(2026, 10, 27)),
            done(rare.id, LocalDate.of(2026, 10, 29)),
            skipped(less.id, LocalDate.of(2026, 10, 28)),
            skipped(less.id, LocalDate.of(2026, 10, 29))
        )

        val summary = calculate(listOf(rare, less, most), completions, StatisticsPeriod.WEEK)!!

        assertEquals(listOf("Бег", "Чтение", "Вода"), summary.rating.map { it.habit.name })
        assertEquals(75, summary.rating.first().percent)
        assertEquals(50, summary.rating[1].percent)
        assertEquals(25, summary.rating.last().percent)
    }

    @Test
    fun doneAndSkippedShareUsesExplicitMarksOnly() {
        val habit = habit(1)
        val completions = listOf(
            done(habit.id, LocalDate.of(2026, 10, 26)),
            done(habit.id, LocalDate.of(2026, 10, 27)),
            done(habit.id, LocalDate.of(2026, 10, 28)),
            skipped(habit.id, LocalDate.of(2026, 10, 29))
        )

        val summary = calculate(listOf(habit), completions, StatisticsPeriod.WEEK)!!

        assertEquals(75, summary.doneSharePercent)
        assertEquals(25, summary.skippedSharePercent)
        assertTrue(summary.hasExplicitCompletions)
    }

    @Test
    fun sharesAreZeroWhenNothingIsMarked() {
        val summary = calculate(listOf(habit(1)), emptyList(), StatisticsPeriod.WEEK)!!

        assertEquals(0, summary.doneSharePercent)
        assertEquals(0, summary.skippedSharePercent)
        assertTrue(!summary.hasExplicitCompletions)
    }

    @Test
    fun maxStreakIsLimitedToSelectedPeriod() {
        val habit = habit(1, createdOn = LocalDate.of(2026, 10, 20))
        val completions = listOf(
            done(habit.id, LocalDate.of(2026, 10, 24)),
            done(habit.id, LocalDate.of(2026, 10, 25)),
            done(habit.id, LocalDate.of(2026, 10, 26)),
            done(habit.id, LocalDate.of(2026, 10, 27)),
            done(habit.id, LocalDate.of(2026, 10, 28))
        )

        val week = calculate(listOf(habit), completions, StatisticsPeriod.WEEK)!!
        val month = calculate(listOf(habit), completions, StatisticsPeriod.MONTH)!!

        assertEquals(3, week.bestMaxStreakHabit?.maxStreak)
        assertEquals(5, week.bestCurrentStreakHabit?.currentStreak)
        assertEquals(5, month.bestMaxStreakHabit?.maxStreak)
    }

    @Test
    fun bestHabitBreaksTieByEarliestCreation() {
        val older = habit(1, name = "Старая", createdOn = LocalDate.of(2026, 10, 1))
        val newer = habit(2, name = "Новая", createdOn = LocalDate.of(2026, 10, 20))
        val completions = listOf(
            done(older.id, LocalDate.of(2026, 10, 26)),
            done(newer.id, LocalDate.of(2026, 10, 27))
        )

        val summary = calculate(listOf(newer, older), completions, StatisticsPeriod.WEEK)!!

        assertEquals("Старая", summary.bestHabit?.habit?.name)
    }

    @Test
    fun inactiveHabitsAreExcluded() {
        val active = habit(1)
        val inactive = habit(2, isActive = false)
        val completions = listOf(
            done(active.id, LocalDate.of(2026, 10, 26)),
            done(inactive.id, LocalDate.of(2026, 10, 26))
        )

        val summary = calculate(listOf(active, inactive), completions, StatisticsPeriod.WEEK)!!

        assertEquals(1, summary.rating.size)
        assertEquals(1, summary.totals.done)
    }

    @Test
    fun noActiveHabitsGivesNoStatistics() {
        assertNull(calculate(listOf(habit(1, isActive = false)), emptyList(), StatisticsPeriod.MONTH))
        assertNull(calculate(emptyList(), emptyList(), StatisticsPeriod.WEEK))
    }
}

