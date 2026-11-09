package com.example.lifetrack.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.example.lifetrack.R
import com.example.lifetrack.domain.model.FrequencyType
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.schedule.ALL_WEEK_DAYS_MASK
import com.example.lifetrack.domain.schedule.isWeekDaySelected
import java.time.DayOfWeek

@Composable
fun habitFrequencyText(habit: Habit): String = when (habit.frequencyType) {
    FrequencyType.DAILY -> stringResource(R.string.frequency_daily)
    FrequencyType.WEEKLY -> weekDaysText(habit.weekDaysMask)
    FrequencyType.INTERVAL -> intervalText(habit.intervalDays)
}

@Composable
fun weekDaysText(mask: Int?): String {
    val dayNames = stringArrayResource(R.array.week_days_short)
    val selected = DayOfWeek.entries.mapIndexedNotNull { index, day ->
        if (isWeekDaySelected(mask, day)) dayNames.getOrNull(index) else null
    }
    return if (mask == null || mask and ALL_WEEK_DAYS_MASK == 0 || selected.isEmpty()) {
        stringResource(R.string.editor_frequency_weekly)
    } else {
        selected.joinToString(", ")
    }
}

@Composable
fun intervalText(intervalDays: Int?): String {
    val days = intervalDays ?: return stringResource(R.string.editor_frequency_interval)
    return pluralStringResource(R.plurals.interval_days_label, days, days)
}
