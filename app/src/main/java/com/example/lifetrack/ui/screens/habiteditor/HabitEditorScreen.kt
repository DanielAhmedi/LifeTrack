package com.example.lifetrack.ui.screens.habiteditor

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifetrack.LifeTrackApplication
import com.example.lifetrack.R
import com.example.lifetrack.domain.model.FrequencyType
import com.example.lifetrack.domain.model.HabitCategory
import com.example.lifetrack.domain.schedule.MAX_INTERVAL_DAYS
import com.example.lifetrack.domain.schedule.MIN_INTERVAL_DAYS
import com.example.lifetrack.domain.schedule.isWeekDaySelected
import com.example.lifetrack.ui.components.EmptyState
import com.example.lifetrack.ui.components.intervalText
import com.example.lifetrack.ui.components.weekDaysText
import com.example.lifetrack.ui.habit.HabitIcons
import com.example.lifetrack.ui.habit.HabitPaletteColors
import com.example.lifetrack.ui.habit.habitColor
import com.example.lifetrack.ui.habit.habitContainerColor
import com.example.lifetrack.ui.habit.habitContentColor
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditorScreen(
    habitId: Long,
    onBack: () -> Unit,
    onFinished: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val container = (context.applicationContext as LifeTrackApplication).container
    val viewModel: HabitEditorViewModel =
        viewModel(factory = HabitEditorViewModel.factory(container, habitId))
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showFrequencyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HabitEditorEvent.Finished -> onFinished(event.messageRes)
                is HabitEditorEvent.Error ->
                    snackbarHostState.showSnackbar(context.getString(event.messageRes))
            }
        }
    }

    val requestBack: () -> Unit = {
        if (state.hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = state.hasUnsavedChanges) { showDiscardDialog = true }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            EditorTopBar(
                titleRes = if (state.isEditing) {
                    R.string.editor_title_edit
                } else {
                    R.string.editor_title_new
                },
                isEditing = state.isEditing,
                saveEnabled = state.isFormValid && !state.isSaving && !state.isLoading,
                isSaving = state.isSaving,
                onBack = requestBack,
                onSave = {
                    if (state.isFrequencyChanged) {
                        showFrequencyDialog = true
                    } else {
                        viewModel.save()
                    }
                },
                onDelete = { showDeleteDialog = true }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when {
            state.isNotFound -> EditorMessage(
                contentPadding = innerPadding,
                title = stringResource(R.string.editor_not_found_title),
                description = "",
                actionLabel = stringResource(R.string.action_back),
                onAction = onBack
            )

            state.isLoadFailed -> EditorMessage(
                contentPadding = innerPadding,
                title = stringResource(R.string.editor_load_error_title),
                description = stringResource(R.string.editor_load_error_description),
                actionLabel = stringResource(R.string.action_retry),
                onAction = viewModel::retry
            )

            state.isLoading -> Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            else -> EditorForm(
                state = state,
                viewModel = viewModel,
                contentPadding = innerPadding
            )
        }
    }

    HabitEditorDialogs(
        showDeleteDialog = showDeleteDialog,
        showDiscardDialog = showDiscardDialog,
        showFrequencyDialog = showFrequencyDialog,
        onDismissDelete = { showDeleteDialog = false },
        onConfirmDelete = {
            showDeleteDialog = false
            viewModel.delete()
        },
        onDismissDiscard = { showDiscardDialog = false },
        onConfirmDiscard = {
            showDiscardDialog = false
            onBack()
        },
        onDismissFrequency = { showFrequencyDialog = false },
        onConfirmFrequency = {
            showFrequencyDialog = false
            viewModel.save()
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTopBar(
    @StringRes titleRes: Int,
    isEditing: Boolean,
    saveEnabled: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(text = stringResource(titleRes), maxLines = 1) },
        navigationIcon = {
            TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(text = stringResource(R.string.action_back))
            }
        },
        actions = {
            if (isEditing) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Text(text = "…", style = MaterialTheme.typography.titleMedium)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.action_delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
            TextButton(
                onClick = onSave,
                enabled = saveEnabled,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = stringResource(R.string.action_save))
                }
            }
        },
        windowInsets = WindowInsets(0)
    )
}


@Composable
private fun HabitEditorDialogs(
    showDeleteDialog: Boolean,
    showDiscardDialog: Boolean,
    showFrequencyDialog: Boolean,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDiscard: () -> Unit,
    onConfirmDiscard: () -> Unit,
    onDismissFrequency: () -> Unit,
    onConfirmFrequency: () -> Unit
) {
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(text = stringResource(R.string.delete_habit_title)) },
            text = { Text(text = stringResource(R.string.delete_habit_message)) },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = onDismissDiscard,
            title = { Text(text = stringResource(R.string.editor_discard_title)) },
            text = { Text(text = stringResource(R.string.editor_discard_message)) },
            confirmButton = {
                TextButton(onClick = onConfirmDiscard) {
                    Text(text = stringResource(R.string.action_leave))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDiscard) {
                    Text(text = stringResource(R.string.action_stay))
                }
            }
        )
    }
    if (showFrequencyDialog) {
        AlertDialog(
            onDismissRequest = onDismissFrequency,
            title = { Text(text = stringResource(R.string.editor_section_frequency)) },
            text = { Text(text = stringResource(R.string.editor_frequency_warning)) },
            confirmButton = {
                TextButton(onClick = onConfirmFrequency) {
                    Text(text = stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissFrequency) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun EditorMessage(
    contentPadding: PaddingValues,
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        EmptyState(
            iconRes = R.drawable.ic_empty_habits,
            title = title,
            description = description,
            actionLabel = actionLabel,
            onAction = onAction
        )
    }
}

@Composable
private fun SectionTitle(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}


@Composable
private fun EditorForm(
    state: HabitEditorUiState,
    viewModel: HabitEditorViewModel,
    contentPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle(R.string.editor_section_main)
        NameField(state = state, onValueChange = viewModel::onNameChange)
        DescriptionField(state = state, onValueChange = viewModel::onDescriptionChange)
        CategoryChips(
            categories = state.categories,
            selectedId = state.categoryId,
            onSelect = viewModel::onCategorySelect
        )
        SectionTitle(R.string.editor_section_appearance)
        IconPicker(selectedKey = state.icon, onSelect = viewModel::onIconSelect)
        ColorPicker(selectedColor = state.color, onSelect = viewModel::onColorSelect)
        HabitPreview(state = state)
        SectionTitle(R.string.editor_section_frequency)
        FrequencyTypeChips(
            selected = state.frequencyType,
            onSelect = viewModel::onFrequencyTypeChange
        )
        when (state.frequencyType) {
            FrequencyType.DAILY -> Unit
            FrequencyType.WEEKLY -> WeekDayChips(
                mask = state.weekDaysMask,
                onToggle = viewModel::toggleWeekDay,
                errorRes = state.weekDaysError
            )

            FrequencyType.INTERVAL -> IntervalInput(state = state, viewModel = viewModel)
        }
        if (state.isFrequencyChanged) {
            WarningPlate()
        }
        if (state.isEditing) {
            SectionTitle(R.string.editor_section_activity)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.editor_active_switch),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.isActive,
                    onCheckedChange = viewModel::onActiveChange
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun NameField(state: HabitEditorUiState, onValueChange: (String) -> Unit) {
    val errorRes = state.nameError
    OutlinedTextField(
        value = state.name,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = stringResource(R.string.editor_name_label)) },
        singleLine = true,
        isError = state.nameTouched && errorRes != null,
        supportingText = {
            if (state.nameTouched && errorRes != null) {
                Text(text = stringResource(errorRes))
            } else {
                Text(
                    text = stringResource(
                        R.string.editor_counter,
                        state.name.length,
                        HabitFormRules.NAME_MAX_LENGTH
                    )
                )
            }
        }
    )
}

@Composable
private fun DescriptionField(state: HabitEditorUiState, onValueChange: (String) -> Unit) {
    val errorRes = state.descriptionError
    OutlinedTextField(
        value = state.description,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = stringResource(R.string.editor_description_label)) },
        minLines = 3,
        maxLines = 5,
        isError = errorRes != null,
        supportingText = {
            if (errorRes != null) {
                Text(text = stringResource(errorRes))
            } else {
                Text(
                    text = stringResource(
                        R.string.editor_counter,
                        state.description.length,
                        HabitFormRules.DESCRIPTION_MAX_LENGTH
                    )
                )
            }
        }
    )
}


@Composable
private fun CategoryChips(
    categories: List<HabitCategory>,
    selectedId: Long?,
    onSelect: (HabitCategory?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item(key = "no_category") {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                label = { Text(text = stringResource(R.string.label_no_category)) }
            )
        }
        items(items = categories, key = { category -> category.id }) { category ->
            FilterChip(
                selected = selectedId == category.id,
                onClick = { onSelect(category) },
                label = { Text(text = category.name) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(habitColor(category.color))
                    )
                }
            )
        }
    }
}

@Composable
private fun WarningPlate() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = stringResource(R.string.editor_frequency_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun FrequencyTypeChips(selected: FrequencyType, onSelect: (FrequencyType) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FrequencyType.entries.forEach { type ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelect(type) },
                label = { Text(text = stringResource(type.labelRes())) }
            )
        }
    }
}

@StringRes
private fun FrequencyType.labelRes(): Int = when (this) {
    FrequencyType.DAILY -> R.string.editor_frequency_daily
    FrequencyType.WEEKLY -> R.string.editor_frequency_weekly
    FrequencyType.INTERVAL -> R.string.editor_frequency_interval
}

@Composable
private fun WeekDayChips(mask: Int, onToggle: (DayOfWeek) -> Unit, @StringRes errorRes: Int?) {
    val dayNames = stringArrayResource(R.array.week_days_short)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DayOfWeek.entries.forEachIndexed { index, day ->
                FilterChip(
                    selected = isWeekDaySelected(mask, day),
                    onClick = { onToggle(day) },
                    label = { Text(text = dayNames.getOrElse(index) { "" }) }
                )
            }
        }
        if (errorRes != null) {
            Text(
                text = stringResource(errorRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}


@Composable
private fun IntervalInput(state: HabitEditorUiState, viewModel: HabitEditorViewModel) {
    val decreaseDescription = stringResource(R.string.editor_interval_decrease)
    val increaseDescription = stringResource(R.string.editor_interval_increase)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = viewModel::decreaseInterval,
                enabled = state.intervalDays > MIN_INTERVAL_DAYS,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = decreaseDescription }
            ) {
                Text(text = "−", style = MaterialTheme.typography.titleMedium)
            }
            OutlinedTextField(
                value = state.intervalDays.toString(),
                onValueChange = { value ->
                    val digits = value.filter { it.isDigit() }.take(2)
                    viewModel.onIntervalChange(digits.toIntOrNull() ?: 0)
                },
                modifier = Modifier.width(96.dp),
                label = { Text(text = stringResource(R.string.editor_interval_label)) },
                singleLine = true,
                isError = state.intervalError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedButton(
                onClick = viewModel::increaseInterval,
                enabled = state.intervalDays < MAX_INTERVAL_DAYS,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = increaseDescription }
            ) {
                Text(text = "+", style = MaterialTheme.typography.titleMedium)
            }
        }
        val errorRes = state.intervalError
        if (errorRes != null) {
            Text(
                text = stringResource(errorRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                text = pluralStringResource(
                    R.plurals.interval_days_label,
                    state.intervalDays,
                    state.intervalDays
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IconPicker(selectedKey: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HabitIcons.keys.withIndex().chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    IconOption(
                        key = item.value,
                        index = item.index,
                        selected = item.value == selectedKey,
                        onSelect = onSelect
                    )
                }
                repeat(5 - row.size) {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
private fun IconOption(
    key: String,
    index: Int,
    selected: Boolean,
    onSelect: (String) -> Unit
) {
    val description = stringResource(R.string.editor_icon_option, index + 1)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .then(
                if (selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = { onSelect(key) }
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = HabitIcons.drawableRes(key)),
            contentDescription = null,
            tint = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(24.dp)
        )
    }
}


@Composable
private fun ColorPicker(selectedColor: Int, onSelect: (Int) -> Unit) {
    val colorNames = stringArrayResource(R.array.habit_palette_names)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HabitPaletteColors.withIndex().chunked(6).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { item ->
                    ColorOption(
                        color = item.value,
                        description = colorNames.getOrNull(item.index).orEmpty(),
                        selected = item.value == selectedColor,
                        onSelect = onSelect
                    )
                }
                repeat(6 - row.size) {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: Int,
    description: String,
    selected: Boolean,
    onSelect: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = { onSelect(color) }
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(color))
                .then(
                    if (selected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = null,
                    tint = habitContentColor(color),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HabitPreview(state: HabitEditorUiState) {
    val frequencyText = when (state.frequencyType) {
        FrequencyType.DAILY -> stringResource(R.string.frequency_daily)
        FrequencyType.WEEKLY -> weekDaysText(state.weekDaysMask)
        FrequencyType.INTERVAL -> intervalText(state.intervalDays)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(habitContainerColor(state.color)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = HabitIcons.drawableRes(state.icon)),
                    contentDescription = null,
                    tint = habitColor(state.color),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = state.name.ifBlank { stringResource(R.string.editor_preview_name) },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (state.description.isNotBlank()) {
                    Text(
                        text = state.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = frequencyText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

