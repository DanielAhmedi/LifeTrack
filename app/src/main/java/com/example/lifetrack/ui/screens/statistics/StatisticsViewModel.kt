package com.example.lifetrack.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifetrack.di.AppContainer
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.model.HabitCompletion
import com.example.lifetrack.domain.repository.HabitCompletionRepository
import com.example.lifetrack.domain.repository.HabitRepository
import com.example.lifetrack.domain.statistics.StatisticsPeriod
import com.example.lifetrack.domain.statistics.StatisticsRange
import com.example.lifetrack.domain.statistics.calculateStatistics
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

private val RangeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru"))

private data class StatisticsData(
    val habits: List<Habit> = emptyList(),
    val completions: List<HabitCompletion> = emptyList(),
    val isError: Boolean = false
)

class StatisticsViewModel(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository
) : ViewModel() {

    private val selectedPeriod = MutableStateFlow(StatisticsPeriod.WEEK)
    private val reloadTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val data: Flow<StatisticsData> =
        combine(selectedPeriod, reloadTrigger) { period, _ -> period }
            .flatMapLatest {
                combine(
                    habitRepository.observeActive(),
                    habitCompletionRepository.observeAll()
                ) { habits, completions -> StatisticsData(habits, completions) }
                    .catch { emit(StatisticsData(isError = true)) }
            }

    val uiState: StateFlow<StatisticsUiState> =
        combine(selectedPeriod, data) { period, currentData -> buildState(period, currentData) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = StatisticsUiState()
            )

    fun selectPeriod(period: StatisticsPeriod) {
        selectedPeriod.value = period
    }

    fun retry() {
        reloadTrigger.value += 1
    }

    private fun buildState(period: StatisticsPeriod, data: StatisticsData): StatisticsUiState {
        if (data.isError) {
            return StatisticsUiState(isLoading = false, isError = true, period = period)
        }
        val summary = calculateStatistics(
            habits = data.habits,
            completions = data.completions,
            period = period,
            today = LocalDate.now()
        )
        return StatisticsUiState(
            isLoading = false,
            isError = false,
            period = period,
            summary = summary,
            rangeLabel = summary?.let { formatRange(it.range) }.orEmpty()
        )
    }

    private fun formatRange(range: StatisticsRange): String {
        val start = range.start.format(RangeFormatter)
        val end = range.end.format(RangeFormatter)
        return if (range.start == range.end) end else "$start — $end"
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    StatisticsViewModel(
                        habitRepository = container.habitRepository,
                        habitCompletionRepository = container.habitCompletionRepository
                    )
                }
            }
    }
}
