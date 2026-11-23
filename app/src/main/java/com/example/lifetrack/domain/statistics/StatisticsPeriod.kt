package com.example.lifetrack.domain.statistics

import java.time.DayOfWeek
import java.time.LocalDate

enum class StatisticsPeriod {
    WEEK,
    MONTH,
    ALL
}

data class StatisticsRange(
    val start: LocalDate,
    val end: LocalDate
)

fun periodStartDate(
    period: StatisticsPeriod,
    today: LocalDate,
    earliestCreatedOn: LocalDate?
): LocalDate? = when (period) {
    StatisticsPeriod.WEEK -> today.with(DayOfWeek.MONDAY).coerceAtMost(today)
    StatisticsPeriod.MONTH -> today.withDayOfMonth(1)
    StatisticsPeriod.ALL -> earliestCreatedOn
}
