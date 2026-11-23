package com.example.lifetrack.ui.screens.statistics

import com.example.lifetrack.domain.statistics.StatisticsPeriod
import com.example.lifetrack.domain.statistics.StatisticsSummary

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val period: StatisticsPeriod = StatisticsPeriod.WEEK,
    val summary: StatisticsSummary? = null,
    val rangeLabel: String = ""
)
