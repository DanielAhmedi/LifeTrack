package com.example.lifetrack.domain.schedule

import com.example.lifetrack.domain.model.FrequencyType
import com.example.lifetrack.domain.model.Habit
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitScheduleTest {

    private val zone: ZoneId = ZoneId.of("UTC")
    private val createdOn: LocalDate = LocalDate.of(2026, 10, 1)

    private fun habit(
        frequencyType: FrequencyType,
        weekDaysMask: Int? = null,
        intervalDays: Int? = null
    ): Habit = Habit(
        id = 1L,
        name = "Привычка",
        icon = "star",
        color = 0xFF3D9A50.toInt(),
        createdAt = createdOn.atStartOfDay(zone).toInstant().toEpochMilli(),
        frequencyType = frequencyType,
        weekDaysMask = weekDaysMask,
        intervalDays = intervalDays
    )

    @Test
    fun dailyHabitIsScheduledEveryDayFromCreation() {
        val habit = habit(FrequencyType.DAILY)

        assertTrue(habit.isScheduledOn(createdOn, createdOn))
        assertTrue(habit.isScheduledOn(createdOn.plusDays(5), createdOn))
        assertFalse(habit.isScheduledOn(createdOn.minusDays(1), createdOn))
    }

    @Test
    fun weeklyHabitUsesWeekDaysMask() {
        val monday = LocalDate.of(2026, 10, 5)
        val tuesday = LocalDate.of(2026, 10, 6)
        val wednesday = LocalDate.of(2026, 10, 7)
        val friday = LocalDate.of(2026, 10, 9)
        val mask = weekDayBit(java.time.DayOfWeek.MONDAY) or
            weekDayBit(java.time.DayOfWeek.WEDNESDAY) or
            weekDayBit(java.time.DayOfWeek.FRIDAY)
        val habit = habit(FrequencyType.WEEKLY, weekDaysMask = mask)

        assertTrue(habit.isScheduledOn(monday, createdOn))
        assertFalse(habit.isScheduledOn(tuesday, createdOn))
        assertTrue(habit.isScheduledOn(wednesday, createdOn))
        assertTrue(habit.isScheduledOn(friday, createdOn))
        assertFalse(habit.isScheduledOn(tuesday.plusWeeks(1), createdOn))
    }

    @Test
    fun weeklyHabitWithoutMaskIsNotScheduled() {
        val habit = habit(FrequencyType.WEEKLY, weekDaysMask = null)

        assertFalse(habit.isScheduledOn(createdOn.plusDays(3), createdOn))
    }

    @Test
    fun intervalHabitIsScheduledEveryNthDay() {
        val habit = habit(FrequencyType.INTERVAL, intervalDays = 3)

        assertTrue(habit.isScheduledOn(createdOn, createdOn))
        assertFalse(habit.isScheduledOn(createdOn.plusDays(1), createdOn))
        assertFalse(habit.isScheduledOn(createdOn.plusDays(2), createdOn))
        assertTrue(habit.isScheduledOn(createdOn.plusDays(3), createdOn))
        assertTrue(habit.isScheduledOn(createdOn.plusDays(6), createdOn))
    }

    @Test
    fun intervalHabitWithInvalidIntervalIsNotScheduled() {
        val tooSmall = habit(FrequencyType.INTERVAL, intervalDays = 1)
        val tooBig = habit(FrequencyType.INTERVAL, intervalDays = 31)
        val missing = habit(FrequencyType.INTERVAL, intervalDays = null)

        assertFalse(tooSmall.isScheduledOn(createdOn, createdOn))
        assertFalse(tooBig.isScheduledOn(createdOn.plusDays(31), createdOn))
        assertFalse(missing.isScheduledOn(createdOn, createdOn))
    }

    @Test
    fun habitIsNeverScheduledBeforeCreationDate() {
        val daily = habit(FrequencyType.DAILY)
        val weekly = habit(FrequencyType.WEEKLY, weekDaysMask = ALL_WEEK_DAYS_MASK)
        val interval = habit(FrequencyType.INTERVAL, intervalDays = 2)

        assertFalse(daily.isScheduledOn(createdOn.minusDays(1), createdOn))
        assertFalse(weekly.isScheduledOn(createdOn.minusWeeks(1), createdOn))
        assertFalse(interval.isScheduledOn(createdOn.minusDays(2), createdOn))
    }

    @Test
    fun creationDateIsDerivedFromCreatedAtInGivenZone() {
        val habit = habit(FrequencyType.DAILY)

        assertTrue(habit.creationDate(zone) == createdOn)
    }
}
