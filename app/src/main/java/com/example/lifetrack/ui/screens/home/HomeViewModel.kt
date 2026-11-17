package com.example.lifetrack.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifetrack.R
import com.example.lifetrack.di.AppContainer
import com.example.lifetrack.domain.model.CompletionStatus
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.model.HabitCategory
import com.example.lifetrack.domain.model.HabitCompletion
import com.example.lifetrack.domain.progress.DayProgress
import com.example.lifetrack.domain.progress.HabitStreak
import com.example.lifetrack.domain.progress.calculateDayProgress
import com.example.lifetrack.domain.progress.calculateHabitStreak
import com.example.lifetrack.domain.repository.HabitCategoryRepository
import com.example.lifetrack.domain.repository.HabitCompletionRepository
import com.example.lifetrack.domain.repository.HabitRepository
import com.example.lifetrack.domain.schedule.isScheduledOn
import java.time.LocalDate
import java.time.ZoneId
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

private data class HomeData(
    val habits: List<Habit>,
    val categories: List<HabitCategory>,
    val completions: List<HabitCompletion>,
    val isError: Boolean = false
)

class HomeViewModel(
    private val habitRepository: HabitRepository,
    private val habitCategoryRepository: HabitCategoryRepository,
    private val habitCompletionRepository: HabitCompletionRepository
) : ViewModel() {

    private val mode = MutableStateFlow(HomeMode.TODAY)
    private val reloadTrigger = MutableStateFlow(0)
    private val messagesFlow = MutableSharedFlow<Int>(extraBufferCapacity = 4)

    val messages: SharedFlow<Int> = messagesFlow.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val data: Flow<HomeData> = reloadTrigger.flatMapLatest {
        combine(
            habitRepository.observeAll(),
            habitCategoryRepository.observeAll(),
            habitCompletionRepository.observeAll()
        ) { habits, categories, completions ->
            HomeData(habits = habits, categories = categories, completions = completions)
        }.catch {
            emit(
                HomeData(
                    habits = emptyList(),
                    categories = emptyList(),
                    completions = emptyList(),
                    isError = true
                )
            )
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(mode, data) { currentMode, currentData ->
        buildState(currentMode, currentData)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState()
    )

    fun setMode(newMode: HomeMode) {
        mode.value = newMode
    }

    fun retry() {
        reloadTrigger.value += 1
    }

    fun toggleActive(habit: Habit) {
        viewModelScope.launch {
            val updated = habit.copy(isActive = !habit.isActive)
            runCatching { habitRepository.update(updated) }
                .onSuccess {
                    messagesFlow.emit(
                        if (updated.isActive) R.string.habit_resumed else R.string.habit_paused
                    )
                }
                .onFailure { messagesFlow.emit(R.string.habit_action_failed) }
        }
    }

    fun delete(habit: Habit) {
        viewModelScope.launch {
            runCatching { habitRepository.deleteById(habit.id) }
                .onSuccess { messagesFlow.emit(R.string.habit_deleted) }
                .onFailure { messagesFlow.emit(R.string.habit_action_failed) }
        }
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

    private fun buildState(currentMode: HomeMode, data: HomeData): HomeUiState {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val todayEpochDay = today.toEpochDay()
        val todayStatuses = data.completions
            .filter { it.date == todayEpochDay }
            .associate { it.habitId to it.status }
        val completionsByHabit = data.completions.groupBy { it.habitId }
        val scheduledToday = data.habits.filter { it.isActive && it.isScheduledOn(today) }
        val visible = if (currentMode == HomeMode.TODAY) scheduledToday else data.habits
        val streaks: Map<Long, HabitStreak> = data.habits.associate { habit ->
            habit.id to calculateHabitStreak(
                habit = habit,
                completions = completionsByHabit[habit.id].orEmpty(),
                today = today,
                zone = zone
            )
        }
        val progress: DayProgress = calculateDayProgress(
            scheduledToday.map { habit -> todayStatuses[habit.id] }
        )
        return HomeUiState(
            isLoading = false,
            isError = data.isError,
            mode = currentMode,
            groups = if (data.isError) emptyList() else groupHabits(visible, data.categories),
            todayStatuses = if (data.isError) emptyMap() else todayStatuses,
            streaks = if (data.isError) emptyMap() else streaks,
            progress = if (data.isError) DayProgress() else progress,
            totalCount = data.habits.size,
            activeCount = data.habits.count { it.isActive },
            pausedCount = data.habits.count { !it.isActive }
        )
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    habitRepository = container.habitRepository,
                    habitCategoryRepository = container.habitCategoryRepository,
                    habitCompletionRepository = container.habitCompletionRepository
                )
            }
        }
    }
}
