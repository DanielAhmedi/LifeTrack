package com.example.lifetrack.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lifetrack.R
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.ui.habit.HabitIcons
import com.example.lifetrack.ui.habit.habitColor
import com.example.lifetrack.ui.habit.habitContainerColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitCard(
    habit: Habit,
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val menuDescription = stringResource(R.string.action_habit_menu)
    val description = habit.description?.takeIf { it.isNotBlank() }
    val containerColor = if (habit.isActive) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }
    val titleColor = if (habit.isActive) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true }
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(habitContainerColor(habit.color)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = HabitIcons.drawableRes(habit.icon)),
                    contentDescription = null,
                    tint = habitColor(habit.color),
                    modifier = Modifier.size(24.dp)
                )
            }
            HabitCardTexts(
                habit = habit,
                description = description,
                titleColor = titleColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            HabitCardMenu(
                habit = habit,
                menuDescription = menuDescription,
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it },
                onEdit = onClick,
                onToggleActive = onToggleActive,
                onDelete = onDelete
            )
        }
    }
}


@Composable
private fun HabitCardTexts(
    habit: Habit,
    description: String?,
    titleColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = habit.name,
            style = MaterialTheme.typography.titleMedium,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = habitFrequencyText(habit),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!habit.isActive) {
                Text(
                    text = stringResource(R.string.label_paused),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HabitCardMenu(
    habit: Habit,
    menuDescription: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Box {
        IconButton(
            onClick = { onExpandedChange(true) },
            modifier = Modifier.semantics { contentDescription = menuDescription }
        ) {
            Text(text = "…", style = MaterialTheme.typography.titleMedium)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.action_edit)) },
                onClick = {
                    onExpandedChange(false)
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            if (habit.isActive) R.string.action_pause else R.string.action_resume
                        )
                    )
                },
                onClick = {
                    onExpandedChange(false)
                    onToggleActive()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    onExpandedChange(false)
                    onDelete()
                }
            )
        }
    }
}
