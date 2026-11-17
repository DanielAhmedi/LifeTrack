package com.example.lifetrack.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifetrack.R
import com.example.lifetrack.di.AppContainer
import com.example.lifetrack.domain.model.CompletionStatus
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.model.HabitCompletion
import com.example.lifetrack.domain.repository.HabitCompletionRepository
import com.example.lifetrack.domain.repository.HabitRepository
import com.example.lifetrack.domain.schedule.creationDate
import com.example.lifetrack.domain.schedule.isScheduledOn
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class CalendarData(
    val habits: List<Habit>,
    val completions: List<HabitCompletion>,
    val isError: Boolean = false
)

class CalendarViewModel(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val selectedHabitId = MutableStateFlow<Long?>(null)
    private val selectedDate = MutableStateFlow<LocalDate?>(null)
    private val reloadTrigger = MutableStateFlow(0)
    private val messagesFlow = MutableSharedFlow<Int>(extraBufferCapacity = 4)

    val messages: SharedFlow<Int> = messagesFlow.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val data: Flow<CalendarData> =
        combine(selectedMonth, reloadTrigger) { month, _ -> month }
            .flatMapLatest { month ->
                combine(
                    habitRepository.observeAll(),
                    habitCompletionRepository.observeBetween(
                        month.atDay(1).toEpochDay(),
                        month.atEndOfMonth().toEpochDay()
                    )
                ) { habits, completions -> CalendarData(habits, completions) }
                    .catch { emit(CalendarData(emptyList(), emptyList(), isError = true)) }
            }

    val uiState: StateFlow<CalendarUiState> = combine(
        selectedMonth,
        selectedHabitId,
        selectedDate,
        data
    ) { month, habitId, date, currentData ->
        buildState(month, habitId, date, currentData)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = CalendarUiState()
    )

    fun showPreviousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
        selectedDate.value = null
    }

    fun showNextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
        selectedDate.value = null
    }

    fun showToday() {
        selectedMonth.value = YearMonth.now()
        selectedDate.value = null
    }

    fun selectHabit(habitId: Long?) {
        selectedHabitId.value = habitId
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun dismissDay() {
        selectedDate.value = null
    }

    fun retry() {
        reloadTrigger.value += 1
    }

    fun toggleDone(habit: Habit) {
        if (!canMarkToday(habit)) return
        viewModelScope.launch {
            runCatching {
                val today = LocalDate.now().toEpochDay()
                val existing = habitCompletionRepository.getByHabitAndDate(habit.id, today)
                when {
                    existing == null -> insertCompletion(habit.id, today, CompletionStatus.DONE)
                    existing.status == CompletionStatus.DONE ->
                        habitCompletionRepository.deleteById(existing.id)

                    else -> habitCompletionRepository.update(
                        existing.copy(status = CompletionStatus.DONE)
                    )
                }
            }.onFailure { messagesFlow.emit(R.string.completion_failed) }
        }
    }

    fun markSkipped(habit: Habit) {
        if (!canMarkToday(habit)) return
        viewModelScope.launch {
            runCatching {
                val today = LocalDate.now().toEpochDay()
                val existing = habitCompletionRepository.getByHabitAndDate(habit.id, today)
                if (existing == null) {
                    insertCompletion(habit.id, today, CompletionStatus.SKIPPED)
                } else {
                    habitCompletionRepository.update(
                        existing.copy(status = CompletionStatus.SKIPPED)
                    )
                }
            }.onFailure { messagesFlow.emit(R.string.completion_failed) }
        }
    }

    fun clearCompletion(habit: Habit) {
        if (!canMarkToday(habit)) return
        viewModelScope.launch {
            runCatching {
                val today = LocalDate.now().toEpochDay()
                habitCompletionRepository.getByHabitAndDate(habit.id, today)?.let { existing ->
                    habitCompletionRepository.deleteById(existing.id)
                }
            }.onFailure { messagesFlow.emit(R.string.completion_failed) }
        }
    }

    private suspend fun insertCompletion(habitId: Long, date: Long, status: CompletionStatus) {
        habitCompletionRepository.insert(
            HabitCompletion(habitId = habitId, date = date, status = status)
        )
    }

    private fun canMarkToday(habit: Habit): Boolean {
        val today = LocalDate.now()
        return habit.isActive && habit.isScheduledOn(today)
    }

    private fun buildState(
        month: YearMonth,
        habitId: Long?,
        date: LocalDate?,
        data: CalendarData
    ): CalendarUiState {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val statuses: Map<Pair<Long, LocalDate>, CompletionStatus> = data.completions.associate {
            (it.habitId to LocalDate.ofEpochDay(it.date)) to it.status
        }
        val monthHabits = if (habitId == null) {
            data.habits.filter { it.isActive }
        } else {
            data.habits.filter { it.id == habitId }
        }
        val cells = buildMonthCells(month, monthHabits, statuses, today, date, zone)
        val monthHabitIds = monthHabits.map { it.id }.toSet()
        return CalendarUiState(
            isLoading = false,
            isError = data.isError,
            month = month,
            monthTitle = monthTitle(month),
            weeks = cells.chunked(DAYS_IN_WEEK),
            habits = data.habits.sortedBy { it.name.lowercase() },
            selectedHabitId = habitId,
            selectedDate = date,
            dayHabits = if (date == null) {
                emptyList()
            } else {
                buildDayHabits(date, monthHabits, statuses, zone)
            },
            hasMarksInMonth = data.completions.any { it.habitId in monthHabitIds },
            today = today
        )
    }

    private fun buildMonthCells(
        month: YearMonth,
        habits: List<Habit>,
        statuses: Map<Pair<Long, LocalDate>, CompletionStatus>,
        today: LocalDate,
        selectedDate: LocalDate?,
        zone: ZoneId
    ): List<CalendarDay?> {
        val cells = mutableListOf<CalendarDay?>()
        val leadingBlanks = month.atDay(1).dayOfWeek.value - 1
        repeat(leadingBlanks) { cells += null }
        for (dayOfMonth in 1..month.lengthOfMonth()) {
            val date = month.atDay(dayOfMonth)
            val relevant = habits.filter {
                it.isScheduledOn(date, it.creationDate(zone)) || statuses[it.id to date] != null
            }
            val done = relevant.count { statuses[it.id to date] == CompletionStatus.DONE }
            val skipped = relevant.count { statuses[it.id to date] == CompletionStatus.SKIPPED }
            val state = when {
                relevant.isEmpty() -> CalendarDayState.NONE
                done == relevant.size -> CalendarDayState.COMPLETE
                done > 0 -> CalendarDayState.PARTIAL
                skipped > 0 -> CalendarDayState.SKIPPED
                else -> CalendarDayState.NOT_MARKED
            }
            cells += CalendarDay(
                date = date,
                state = state,
                done = done,
                skipped = skipped,
                expected = relevant.size,
                isToday = date == today,
                isSelected = date == selectedDate,
                isFuture = date.isAfter(today)
            )
        }
        while (cells.size % DAYS_IN_WEEK != 0) cells += null
        while (cells.size < CALENDAR_CELLS) cells += null
        return cells
    }

    private fun buildDayHabits(
        date: LocalDate,
        habits: List<Habit>,
        statuses: Map<Pair<Long, LocalDate>, CompletionStatus>,
        zone: ZoneId
    ): List<CalendarHabitStatus> = habits
        .mapNotNull { habit ->
            val status = statuses[habit.id to date]
            val isScheduled = habit.isScheduledOn(date, habit.creationDate(zone))
            if (isScheduled || status != null) {
                CalendarHabitStatus(habit = habit, status = status, isScheduled = isScheduled)
            } else {
                null
            }
        }
        .sortedBy { it.habit.name.lowercase() }

    private fun monthTitle(month: YearMonth): String {
        val raw = MONTH_TITLE_FORMATTER.format(month.atDay(1))
        return raw.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(MONTH_LOCALE) else char.toString()
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L
        private const val DAYS_IN_WEEK = 7
        private const val CALENDAR_CELLS = 42
        private val MONTH_LOCALE: Locale = Locale.forLanguageTag("ru")
        private val MONTH_TITLE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("LLLL yyyy", MONTH_LOCALE)

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CalendarViewModel(
                    habitRepository = container.habitRepository,
                    habitCompletionRepository = container.habitCompletionRepository
                )
            }
        }
    }
}
