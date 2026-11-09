package com.example.lifetrack.ui.screens.habiteditor

import androidx.annotation.StringRes
import com.example.lifetrack.R
import com.example.lifetrack.domain.model.FrequencyType
import com.example.lifetrack.domain.model.HabitCategory
import com.example.lifetrack.domain.schedule.ALL_WEEK_DAYS_MASK
import com.example.lifetrack.domain.schedule.MAX_INTERVAL_DAYS
import com.example.lifetrack.domain.schedule.MIN_INTERVAL_DAYS
import com.example.lifetrack.ui.habit.DefaultHabitColor
import com.example.lifetrack.ui.habit.HabitIcons

const val DEFAULT_INTERVAL_DAYS = 3

data class HabitDraft(
    val name: String,
    val description: String,
    val categoryId: Long?,
    val icon: String,
    val color: Int,
    val frequencyType: FrequencyType,
    val weekDaysMask: Int,
    val intervalDays: Int,
    val isActive: Boolean
)

val EmptyHabitDraft = HabitDraft(
    name = "",
    description = "",
    categoryId = null,
    icon = HabitIcons.DEFAULT_KEY,
    color = DefaultHabitColor,
    frequencyType = FrequencyType.DAILY,
    weekDaysMask = ALL_WEEK_DAYS_MASK,
    intervalDays = DEFAULT_INTERVAL_DAYS,
    isActive = true
)

data class HabitEditorUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isNotFound: Boolean = false,
    val isLoadFailed: Boolean = false,
    val categories: List<HabitCategory> = emptyList(),
    val name: String = "",
    val description: String = "",
    val categoryId: Long? = null,
    val icon: String = HabitIcons.DEFAULT_KEY,
    val color: Int = DefaultHabitColor,
    val colorChangedByUser: Boolean = false,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val initialFrequencyType: FrequencyType? = null,
    val weekDaysMask: Int = ALL_WEEK_DAYS_MASK,
    val intervalDays: Int = DEFAULT_INTERVAL_DAYS,
    val isActive: Boolean = true,
    val nameTouched: Boolean = false,
    val savedDraft: HabitDraft? = null
) {
    val isFrequencyChanged: Boolean
        get() = initialFrequencyType != null && initialFrequencyType != frequencyType

    val draft: HabitDraft
        get() = HabitDraft(
            name = name.trim(),
            description = description.trim(),
            categoryId = categoryId,
            icon = icon,
            color = color,
            frequencyType = frequencyType,
            weekDaysMask = weekDaysMask,
            intervalDays = intervalDays,
            isActive = isActive
        )

    val hasUnsavedChanges: Boolean
        get() = draft != (savedDraft ?: EmptyHabitDraft)
}

object HabitFormRules {

    const val NAME_MAX_LENGTH = 60
    const val DESCRIPTION_MAX_LENGTH = 255

    fun isNameValid(name: String): Boolean = name.trim().length in 1..NAME_MAX_LENGTH

    fun isDescriptionValid(description: String): Boolean =
        description.length <= DESCRIPTION_MAX_LENGTH

    fun isWeekDaysValid(mask: Int): Boolean = mask and ALL_WEEK_DAYS_MASK != 0

    fun isIntervalValid(intervalDays: Int): Boolean =
        intervalDays in MIN_INTERVAL_DAYS..MAX_INTERVAL_DAYS

    fun isFormValid(state: HabitEditorUiState): Boolean =
        isNameValid(state.name) &&
            isDescriptionValid(state.description) &&
            when (state.frequencyType) {
                FrequencyType.DAILY -> true
                FrequencyType.WEEKLY -> isWeekDaysValid(state.weekDaysMask)
                FrequencyType.INTERVAL -> isIntervalValid(state.intervalDays)
            }
}

val HabitEditorUiState.isFormValid: Boolean
    get() = HabitFormRules.isFormValid(this)

@get:StringRes
val HabitEditorUiState.nameError: Int?
    get() = when {
        name.trim().isEmpty() -> R.string.editor_error_name_required
        name.trim().length > HabitFormRules.NAME_MAX_LENGTH -> R.string.editor_error_name_too_long
        else -> null
    }

@get:StringRes
val HabitEditorUiState.descriptionError: Int?
    get() = if (description.length > HabitFormRules.DESCRIPTION_MAX_LENGTH) {
        R.string.editor_error_description_too_long
    } else {
        null
    }

@get:StringRes
val HabitEditorUiState.weekDaysError: Int?
    get() = if (frequencyType == FrequencyType.WEEKLY &&
        !HabitFormRules.isWeekDaysValid(weekDaysMask)
    ) {
        R.string.editor_error_choose_day
    } else {
        null
    }

@get:StringRes
val HabitEditorUiState.intervalError: Int?
    get() = if (frequencyType == FrequencyType.INTERVAL &&
        !HabitFormRules.isIntervalValid(intervalDays)
    ) {
        R.string.editor_error_interval
    } else {
        null
    }
