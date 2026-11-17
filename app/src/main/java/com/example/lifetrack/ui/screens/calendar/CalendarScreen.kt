package com.example.lifetrack.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifetrack.LifeTrackApplication
import com.example.lifetrack.R
import com.example.lifetrack.domain.model.CompletionStatus
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.ui.components.EmptyState
import com.example.lifetrack.ui.components.HabitCompletionCircle
import com.example.lifetrack.ui.habit.HabitIcons
import com.example.lifetrack.ui.habit.habitColor
import com.example.lifetrack.ui.habit.habitContainerColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DayTitleFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale.forLanguageTag("ru"))

private val DayLabelFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onOpenSettings: () -> Unit,
    onCreateHabit: () -> Unit,
    onEditHabit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val container = (context.applicationContext as LifeTrackApplication).container
    val viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.factory(container))
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { resId ->
            snackbarHostState.showSnackbar(resources.getString(resId))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { CalendarTopBar(onOpenSettings = onOpenSettings) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                uiState.isError -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        iconRes = R.drawable.ic_empty_habits,
                        title = stringResource(R.string.calendar_load_error_title),
                        description = stringResource(R.string.calendar_load_error_description),
                        actionLabel = stringResource(R.string.action_retry),
                        onAction = viewModel::retry
                    )
                }

                uiState.isLoading -> CalendarSkeleton()
                !uiState.hasHabits -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        iconRes = R.drawable.ic_empty_habits,
                        title = stringResource(R.string.calendar_empty_title),
                        description = stringResource(R.string.calendar_empty_description),
                        actionLabel = stringResource(R.string.action_create_habit),
                        onAction = onCreateHabit
                    )
                }

                else -> CalendarContent(
                    state = uiState,
                    onPreviousMonth = viewModel::showPreviousMonth,
                    onNextMonth = viewModel::showNextMonth,
                    onToday = viewModel::showToday,
                    onSelectHabit = viewModel::selectHabit,
                    onSelectDate = viewModel::selectDate
                )
            }
        }
    }

    val selectedDate = uiState.selectedDate
    if (selectedDate != null) {
        DayBottomSheet(
            state = uiState,
            date = selectedDate,
            onDismiss = viewModel::dismissDay,
            onOpenHabit = onEditHabit,
            onToggleCompletion = viewModel::toggleDone,
            onMarkSkipped = viewModel::markSkipped,
            onClearCompletion = viewModel::clearCompletion
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopBar(onOpenSettings: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.nav_calendar),
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            TextButton(onClick = onOpenSettings) {
                Text(text = stringResource(R.string.settings_title))
            }
        },
        windowInsets = WindowInsets(0)
    )
}

@Composable
private fun CalendarContent(
    state: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onSelectHabit: (Long?) -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        MonthPanel(
            monthTitle = state.monthTitle,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onToday = onToday
        )
        HabitFilterRow(
            habits = state.habits,
            selectedHabitId = state.selectedHabitId,
            onSelectHabit = onSelectHabit
        )
        WeekDayHeader()
        MonthGrid(weeks = state.weeks, onSelectDate = onSelectDate)
        CalendarLegend()
        if (!state.hasMarksInMonth) {
            Text(
                text = stringResource(R.string.calendar_has_no_marks),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun MonthPanel(
    monthTitle: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit
) {
    val previousDescription = stringResource(R.string.action_previous_month)
    val nextDescription = stringResource(R.string.action_next_month)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.semantics { contentDescription = previousDescription }
        ) {
            Text(text = "‹", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            text = monthTitle,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onNextMonth,
            modifier = Modifier.semantics { contentDescription = nextDescription }
        ) {
            Text(text = "›", style = MaterialTheme.typography.titleLarge)
        }
        TextButton(onClick = onToday) {
            Text(text = stringResource(R.string.action_today))
        }
    }
}

@Composable
private fun HabitFilterRow(
    habits: List<Habit>,
    selectedHabitId: Long?,
    onSelectHabit: (Long?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedHabitId == null,
            onClick = { onSelectHabit(null) },
            label = { Text(text = stringResource(R.string.calendar_filter_all)) }
        )
        habits.forEach { habit ->
            FilterChip(
                selected = habit.id == selectedHabitId,
                onClick = { onSelectHabit(habit.id) },
                label = { Text(text = habit.name) }
            )
        }
    }
}

@Composable
private fun WeekDayHeader() {
    val dayNames = stringArrayResource(R.array.week_days_short)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        dayNames.forEach { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthGrid(
    weeks: List<List<CalendarDay?>>,
    onSelectDate: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            CalendarDayCell(day = day, onClick = onSelectDate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(day: CalendarDay, onClick: (LocalDate) -> Unit) {
    val dateLabel = day.date.format(DayLabelFormatter)
    val description = if (day.expected == 0) {
        stringResource(R.string.calendar_day_description_empty, dateLabel)
    } else {
        stringResource(R.string.calendar_day_description, dateLabel, day.done, day.expected)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .clickable { onClick(day.date) }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .then(
                    if (day.isToday) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .then(
                        if (day.isSelected) {
                            Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                DayMark(day = day)
            }
        }
    }
}

@Composable
private fun DayMark(day: CalendarDay) {
    val numberColor = when {
        day.state == CalendarDayState.COMPLETE -> MaterialTheme.colorScheme.onPrimary
        day.isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        day.state == CalendarDayState.NONE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(
            alpha = 0.5f
        )

        day.isFuture -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(modifier = Modifier.size(30.dp), contentAlignment = Alignment.Center) {
        when (day.state) {
            CalendarDayState.COMPLETE -> Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )

            CalendarDayState.PARTIAL -> CircularProgressIndicator(
                progress = { if (day.expected == 0) 0f else day.done.toFloat() / day.expected },
                modifier = Modifier.matchParentSize(),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            CalendarDayState.SKIPPED -> Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(2.dp, MaterialTheme.colorScheme.error, CircleShape)
            )

            CalendarDayState.NOT_MARKED -> Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )

            CalendarDayState.NONE -> Unit
        }
        if (day.state == CalendarDayState.SKIPPED) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .size(5.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            )
        }
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = numberColor
        )
    }
}

@Composable
private fun CalendarLegend() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            LegendItem(
                state = CalendarDayState.COMPLETE,
                label = stringResource(R.string.calendar_legend_done),
                modifier = Modifier.weight(1f)
            )
            LegendItem(
                state = CalendarDayState.PARTIAL,
                label = stringResource(R.string.calendar_legend_partial),
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            LegendItem(
                state = CalendarDayState.SKIPPED,
                label = stringResource(R.string.calendar_legend_skipped),
                modifier = Modifier.weight(1f)
            )
            LegendItem(
                state = CalendarDayState.NOT_MARKED,
                label = stringResource(R.string.calendar_legend_none),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LegendItem(
    state: CalendarDayState,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(14.dp), contentAlignment = Alignment.Center) {
            when (state) {
                CalendarDayState.COMPLETE -> Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )

                CalendarDayState.PARTIAL -> CircularProgressIndicator(
                    progress = { 0.5f },
                    modifier = Modifier.matchParentSize(),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                CalendarDayState.SKIPPED -> Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(2.dp, MaterialTheme.colorScheme.error, CircleShape)
                )

                else -> Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayBottomSheet(
    state: CalendarUiState,
    date: LocalDate,
    onDismiss: () -> Unit,
    onOpenHabit: (Long) -> Unit,
    onToggleCompletion: (Habit) -> Unit,
    onMarkSkipped: (Habit) -> Unit,
    onClearCompletion: (Habit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = date.format(DayTitleFormatter),
                style = MaterialTheme.typography.titleMedium
            )
            if (state.dayHabits.isEmpty()) {
                Text(
                    text = stringResource(R.string.calendar_no_habits_for_day),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                state.dayHabits.forEach { item ->
                    DayHabitRow(
                        item = item,
                        canMark = state.canMarkSelectedDay,
                        onOpenHabit = onOpenHabit,
                        onToggleCompletion = onToggleCompletion,
                        onMarkSkipped = onMarkSkipped,
                        onClearCompletion = onClearCompletion
                    )
                }
            }
        }
    }
}

@Composable
private fun DayHabitRow(
    item: CalendarHabitStatus,
    canMark: Boolean,
    onOpenHabit: (Long) -> Unit,
    onToggleCompletion: (Habit) -> Unit,
    onMarkSkipped: (Habit) -> Unit,
    onClearCompletion: (Habit) -> Unit
) {
    val statusColor = when (item.status) {
        CompletionStatus.DONE -> MaterialTheme.colorScheme.primary
        CompletionStatus.SKIPPED -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusText = when (item.status) {
        CompletionStatus.DONE -> stringResource(R.string.status_done)
        CompletionStatus.SKIPPED -> stringResource(R.string.status_skipped)
        null -> if (item.isScheduled) {
            stringResource(R.string.status_not_marked)
        } else {
            stringResource(R.string.status_not_scheduled)
        }
    }
    val completionDescription = statusText
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenHabit(item.habit.id) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(habitContainerColor(item.habit.color)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = HabitIcons.drawableRes(item.habit.icon)),
                contentDescription = null,
                tint = habitColor(item.habit.color),
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = item.habit.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )
        }
        if (canMark && item.isScheduled) {
            HabitCompletionCircle(
                status = item.status,
                contentDescription = completionDescription,
                onClick = {
                    if (item.status == CompletionStatus.DONE) {
                        onClearCompletion(item.habit)
                    } else {
                        onToggleCompletion(item.habit)
                    }
                }
            )
            TextButton(
                onClick = {
                    if (item.status == CompletionStatus.SKIPPED) {
                        onClearCompletion(item.habit)
                    } else {
                        onMarkSkipped(item.habit)
                    }
                }
            ) {
                Text(
                    text = stringResource(
                        if (item.status == CompletionStatus.SKIPPED) {
                            R.string.action_clear_short
                        } else {
                            R.string.action_skip
                        }
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun CalendarSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        repeat(6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(7) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}