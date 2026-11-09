package com.example.lifetrack.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifetrack.LifeTrackApplication
import com.example.lifetrack.R
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.ui.components.EmptyState
import com.example.lifetrack.ui.components.HabitCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val NO_CATEGORY_KEY = 0L
private const val COMPLETED_TODAY = 0
private val HomeDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale.forLanguageTag("ru"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onCreateHabit: () -> Unit,
    onEditHabit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    messageRes: Int? = null,
    onMessageShown: () -> Unit = {}
) {
    val context = LocalContext.current
    val container = (context.applicationContext as LifeTrackApplication).container
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { resId ->
            snackbarHostState.showSnackbar(context.getString(resId))
        }
    }

    LaunchedEffect(messageRes) {
        if (messageRes != null) {
            onMessageShown()
            snackbarHostState.showSnackbar(context.getString(messageRes))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { HomeTopBar(onOpenSettings = onOpenSettings) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateHabit) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = stringResource(R.string.action_create_habit)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            HomeModeSelector(mode = uiState.mode, onModeChange = viewModel::setMode)
            HomeContent(
                state = uiState,
                onRetry = viewModel::retry,
                onCreateHabit = onCreateHabit,
                onHabitClick = onEditHabit,
                onToggleActive = viewModel::toggleActive,
                onDeleteRequest = { habitToDelete = it }
            )
        }
    }

    val pendingHabit = habitToDelete
    if (pendingHabit != null) {
        DeleteHabitDialog(
            onDismiss = { habitToDelete = null },
            onConfirm = {
                viewModel.delete(pendingHabit)
                habitToDelete = null
            }
        )
    }
}

@Composable
private fun ColumnScope.HomeContent(
    state: HomeUiState,
    onRetry: () -> Unit,
    onCreateHabit: () -> Unit,
    onHabitClick: (Long) -> Unit,
    onToggleActive: (Habit) -> Unit,
    onDeleteRequest: (Habit) -> Unit
) {
    when {
        state.isError -> Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            EmptyState(
                iconRes = R.drawable.ic_empty_habits,
                title = stringResource(R.string.home_load_error_title),
                description = stringResource(R.string.home_load_error_description),
                actionLabel = stringResource(R.string.action_retry),
                onAction = onRetry
            )
        }

        state.isLoading -> HomeLoadingState()
        else -> {
            HomeSummary(state = state)
            if (state.isEmpty) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        iconRes = R.drawable.ic_empty_habits,
                        title = stringResource(
                            if (state.mode == HomeMode.TODAY) {
                                R.string.home_empty_today_title
                            } else {
                                R.string.home_empty_all_title
                            }
                        ),
                        description = stringResource(
                            if (state.mode == HomeMode.TODAY) {
                                R.string.home_empty_today_description
                            } else {
                                R.string.home_empty_all_description
                            }
                        ),
                        actionLabel = stringResource(R.string.action_create_habit),
                        onAction = onCreateHabit
                    )
                }
            } else {
                HabitList(
                    groups = state.groups,
                    onHabitClick = onHabitClick,
                    onToggleActive = onToggleActive,
                    onDeleteRequest = onDeleteRequest,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(onOpenSettings: () -> Unit) {
    val todayLabel = remember { LocalDate.now().format(HomeDateFormatter) }
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.home_mode_today),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = todayLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
private fun HomeModeSelector(mode: HomeMode, onModeChange: (HomeMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HomeMode.entries.forEach { item ->
            FilterChip(
                selected = item == mode,
                onClick = { onModeChange(item) },
                label = { Text(text = stringResource(item.labelRes)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HomeSummary(state: HomeUiState) {
    if (state.mode == HomeMode.ALL) {
        Text(
            text = stringResource(
                R.string.home_summary,
                state.totalCount,
                state.activeCount,
                state.pausedCount
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    } else {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (state.todayPlanned == 0) {
                    Text(
                        text = stringResource(R.string.home_no_habits_today),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    val progress = COMPLETED_TODAY.toFloat() / state.todayPlanned
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(
                                R.string.home_progress,
                                COMPLETED_TODAY,
                                state.todayPlanned
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(progress * 100).toInt()} %",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}


@Composable
private fun HabitList(
    groups: List<HabitGroup>,
    onHabitClick: (Long) -> Unit,
    onToggleActive: (Habit) -> Unit,
    onDeleteRequest: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groups.forEach { group ->
            item(key = "category_${group.category?.id ?: NO_CATEGORY_KEY}") {
                GroupHeader(group = group)
            }
            items(items = group.habits, key = { habit -> habit.id }) { habit ->
                HabitCard(
                    habit = habit,
                    onClick = { onHabitClick(habit.id) },
                    onToggleActive = { onToggleActive(habit) },
                    onDelete = { onDeleteRequest(habit) }
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(group: HabitGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = group.category?.name ?: stringResource(R.string.label_no_category),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = group.habits.size.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeleteHabitDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.delete_habit_title)) },
        text = { Text(text = stringResource(R.string.delete_habit_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.action_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

