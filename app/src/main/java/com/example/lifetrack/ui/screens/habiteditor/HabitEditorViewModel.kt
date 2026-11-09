package com.example.lifetrack.ui.screens.habiteditor

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifetrack.R
import com.example.lifetrack.di.AppContainer
import com.example.lifetrack.domain.model.FrequencyType
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.model.HabitCategory
import com.example.lifetrack.domain.repository.HabitCategoryRepository
import com.example.lifetrack.domain.repository.HabitRepository
import com.example.lifetrack.domain.schedule.ALL_WEEK_DAYS_MASK
import com.example.lifetrack.domain.schedule.MAX_INTERVAL_DAYS
import com.example.lifetrack.domain.schedule.MIN_INTERVAL_DAYS
import com.example.lifetrack.domain.schedule.weekDayBit
import java.time.DayOfWeek
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface HabitEditorEvent {
    data class Finished(@StringRes val messageRes: Int) : HabitEditorEvent
    data class Error(@StringRes val messageRes: Int) : HabitEditorEvent
}

class HabitEditorViewModel(
    private val habitRepository: HabitRepository,
    private val habitCategoryRepository: HabitCategoryRepository,
    private val habitId: Long
) : ViewModel() {

    private val state = MutableStateFlow(
        HabitEditorUiState(
            isEditing = habitId > 0,
            isLoading = habitId > 0
        )
    )

    val uiState: StateFlow<HabitEditorUiState> = state.asStateFlow()

    private val eventsFlow = MutableSharedFlow<HabitEditorEvent>(extraBufferCapacity = 4)

    val events: SharedFlow<HabitEditorEvent> = eventsFlow.asSharedFlow()

    private var original: Habit? = null

    init {
        viewModelScope.launch {
            habitCategoryRepository.observeAll()
                .catch { state.update { current -> current.copy(isLoadFailed = true) } }
                .collect { categories ->
                    state.update { current -> current.copy(categories = categories) }
                }
        }
        if (habitId > 0) {
            loadHabit()
        } else {
            state.update { current -> current.copy(isLoading = false) }
        }
    }

    fun retry() {
        state.update { current -> current.copy(isLoadFailed = false, isLoading = true) }
        if (habitId > 0) {
            loadHabit()
        } else {
            state.update { current -> current.copy(isLoading = false) }
        }
    }

    fun onNameChange(value: String) {
        state.update { current ->
            current.copy(
                name = value.take(HabitFormRules.NAME_MAX_LENGTH),
                nameTouched = true
            )
        }
    }

    fun onDescriptionChange(value: String) {
        state.update { current ->
            current.copy(description = value.take(HabitFormRules.DESCRIPTION_MAX_LENGTH))
        }
    }

    fun onCategorySelect(category: HabitCategory?) {
        state.update { current ->
            current.copy(
                categoryId = category?.id,
                color = if (!current.colorChangedByUser && category != null) {
                    category.color
                } else {
                    current.color
                }
            )
        }
    }

    fun onIconSelect(key: String) {
        state.update { current -> current.copy(icon = key) }
    }

    fun onColorSelect(color: Int) {
        state.update { current -> current.copy(color = color, colorChangedByUser = true) }
    }

    fun onFrequencyTypeChange(type: FrequencyType) {
        state.update { current -> current.copy(frequencyType = type) }
    }

    fun toggleWeekDay(day: DayOfWeek) {
        state.update { current ->
            current.copy(weekDaysMask = current.weekDaysMask xor weekDayBit(day))
        }
    }

    fun onIntervalChange(days: Int) {
        state.update { current -> current.copy(intervalDays = days) }
    }

    fun increaseInterval() {
        state.update { current ->
            current.copy(intervalDays = (current.intervalDays + 1).coerceAtMost(MAX_INTERVAL_DAYS))
        }
    }

    fun decreaseInterval() {
        state.update { current ->
            current.copy(intervalDays = (current.intervalDays - 1).coerceAtLeast(MIN_INTERVAL_DAYS))
        }
    }

    fun onActiveChange(active: Boolean) {
        state.update { current -> current.copy(isActive = active) }
    }

    fun save() {
        val current = state.value
        if (!current.isFormValid || current.isSaving) return
        state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val existing = original
            val habit = Habit(
                id = existing?.id ?: 0L,
                name = current.name.trim(),
                description = current.description.trim().ifBlank { null },
                categoryId = current.categoryId,
                icon = current.icon,
                color = current.color,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                frequencyType = current.frequencyType,
                weekDaysMask = if (current.frequencyType == FrequencyType.WEEKLY) {
                    current.weekDaysMask and ALL_WEEK_DAYS_MASK
                } else {
                    null
                },
                intervalDays = if (current.frequencyType == FrequencyType.INTERVAL) {
                    current.intervalDays
                } else {
                    null
                },
                isActive = current.isActive
            )
            runCatching {
                if (existing == null) {
                    habitRepository.insert(habit)
                } else {
                    habitRepository.update(habit)
                }
            }.onSuccess {
                eventsFlow.emit(
                    HabitEditorEvent.Finished(
                        if (existing == null) R.string.habit_created else R.string.habit_updated
                    )
                )
            }.onFailure {
                state.update { value -> value.copy(isSaving = false) }
                eventsFlow.emit(HabitEditorEvent.Error(R.string.habit_save_failed))
            }
        }
    }

    fun delete() {
        val existing = original ?: return
        viewModelScope.launch {
            runCatching { habitRepository.deleteById(existing.id) }
                .onSuccess {
                    eventsFlow.emit(HabitEditorEvent.Finished(R.string.habit_deleted))
                }
                .onFailure {
                    eventsFlow.emit(HabitEditorEvent.Error(R.string.habit_action_failed))
                }
        }
    }

    private fun loadHabit() {
        viewModelScope.launch {
            runCatching { habitRepository.getById(habitId) }
                .onSuccess { habit ->
                    if (habit == null) {
                        state.update { current ->
                            current.copy(isLoading = false, isNotFound = true)
                        }
                    } else {
                        original = habit
                        val draft = HabitDraft(
                            name = habit.name,
                            description = habit.description.orEmpty(),
                            categoryId = habit.categoryId,
                            icon = habit.icon,
                            color = habit.color,
                            frequencyType = habit.frequencyType,
                            weekDaysMask = habit.weekDaysMask ?: ALL_WEEK_DAYS_MASK,
                            intervalDays = habit.intervalDays ?: DEFAULT_INTERVAL_DAYS,
                            isActive = habit.isActive
                        )
                        state.update { current ->
                            current.copy(
                                isLoading = false,
                                isNotFound = false,
                                name = draft.name,
                                description = draft.description,
                                categoryId = draft.categoryId,
                                icon = draft.icon,
                                color = draft.color,
                                frequencyType = draft.frequencyType,
                                initialFrequencyType = draft.frequencyType,
                                weekDaysMask = draft.weekDaysMask,
                                intervalDays = draft.intervalDays,
                                isActive = draft.isActive,
                                savedDraft = draft
                            )
                        }
                    }
                }
                .onFailure {
                    state.update { current ->
                        current.copy(isLoading = false, isLoadFailed = true)
                    }
                }
        }
    }

    companion object {
        fun factory(container: AppContainer, habitId: Long): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    HabitEditorViewModel(
                        habitRepository = container.habitRepository,
                        habitCategoryRepository = container.habitCategoryRepository,
                        habitId = habitId
                    )
                }
            }
    }
}
