package com.example.lifetrack.domain.schedule

import com.example.lifetrack.domain.model.FrequencyType
import com.example.lifetrack.domain.model.Habit
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

const val ALL_WEEK_DAYS_MASK = 0x7F
const val MIN_INTERVAL_DAYS = 2
const val MAX_INTERVAL_DAYS = 30

fun Habit.creationDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(createdAt).atZone(zone).toLocalDate()

fun weekDayBit(dayOfWeek: DayOfWeek): Int = 1 shl (dayOfWeek.value - 1)

fun isWeekDaySelected(mask: Int?, dayOfWeek: DayOfWeek): Boolean =
    mask != null && mask and weekDayBit(dayOfWeek) != 0

fun Habit.isScheduledOn(date: LocalDate, createdOn: LocalDate = creationDate()): Boolean {
    if (date.isBefore(createdOn)) return false
    return when (frequencyType) {
        FrequencyType.DAILY -> true
        FrequencyType.WEEKLY -> isWeekDaySelected(weekDaysMask, date.dayOfWeek)
        FrequencyType.INTERVAL -> {
            val interval = intervalDays
            interval != null &&
                interval in MIN_INTERVAL_DAYS..MAX_INTERVAL_DAYS &&
                ChronoUnit.DAYS.between(createdOn, date) % interval == 0L
        }
    }
}
