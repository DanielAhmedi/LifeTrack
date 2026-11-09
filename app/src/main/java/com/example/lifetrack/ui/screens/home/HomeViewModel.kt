package com.example.lifetrack.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifetrack.R
import com.example.lifetrack.di.AppContainer
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.model.HabitCategory
import com.example.lifetrack.domain.repository.HabitCategoryRepository
import com.example.lifetrack.domain.repository.HabitRepository
import com.example.lifetrack.domain.schedule.isScheduledOn
import java.time.LocalDate
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
    val isError: Boolean = false
)

class HomeViewModel(
    private val habitRepository: HabitRepository,
    private val habitCategoryRepository: HabitCategoryRepository
) : ViewModel() {

    private val mode = MutableStateFlow(HomeMode.TODAY)
    private val reloadTrigger = MutableStateFlow(0)
    private val messagesFlow = MutableSharedFlow<Int>(extraBufferCapacity = 4)

    val messages: SharedFlow<Int> = messagesFlow.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val data: Flow<HomeData> = reloadTrigger.flatMapLatest {
        combine(
            habitRepository.observeAll(),
            habitCategoryRepository.observeAll()
        ) { habits, categories ->
            HomeData(habits = habits, categories = categories)
        }.catch {
            emit(HomeData(habits = emptyList(), categories = emptyList(), isError = true))
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

    private fun buildState(currentMode: HomeMode, data: HomeData): HomeUiState {
        val today = LocalDate.now()
        val scheduledToday = data.habits.filter { it.isActive && it.isScheduledOn(today) }
        val visible = if (currentMode == HomeMode.TODAY) scheduledToday else data.habits
        return HomeUiState(
            isLoading = false,
            isError = data.isError,
            mode = currentMode,
            groups = if (data.isError) emptyList() else groupHabits(visible, data.categories),
            todayPlanned = scheduledToday.size,
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
                    habitCategoryRepository = container.habitCategoryRepository
                )
            }
        }
    }
}
