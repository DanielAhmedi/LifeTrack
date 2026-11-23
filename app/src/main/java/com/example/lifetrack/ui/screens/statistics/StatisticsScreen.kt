package com.example.lifetrack.ui.screens.statistics

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifetrack.LifeTrackApplication
import com.example.lifetrack.R
import com.example.lifetrack.domain.statistics.HabitPeriodStatistics
import com.example.lifetrack.domain.statistics.StatisticsPeriod
import com.example.lifetrack.domain.statistics.StatisticsSummary
import com.example.lifetrack.domain.statistics.WeekdayDone
import com.example.lifetrack.ui.components.EmptyState
import com.example.lifetrack.ui.habit.HabitIcons
import com.example.lifetrack.ui.habit.habitColor
import com.example.lifetrack.ui.habit.habitContainerColor

private val BarAreaHeight = 96.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onOpenSettings: () -> Unit,
    onCreateHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val container = (context.applicationContext as LifeTrackApplication).container
    val viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.factory(container))
    val uiState by viewModel.uiState.collectAsState()
    var showRules by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            StatisticsTopBar(
                onShowRules = { showRules = true },
                onOpenSettings = onOpenSettings
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            StatisticsPeriodSelector(
                period = uiState.period,
                onPeriodChange = viewModel::selectPeriod
            )
            val summary = uiState.summary
            when {
                uiState.isError -> StatisticsError(onRetry = viewModel::retry)
                uiState.isLoading -> StatisticsSkeleton()
                summary == null -> StatisticsEmpty(onCreateHabit = onCreateHabit)
                else -> StatisticsContent(summary = summary, rangeLabel = uiState.rangeLabel)
            }
        }
    }

    if (showRules) {
        StatisticsRulesDialog(onDismiss = { showRules = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticsTopBar(onShowRules: () -> Unit, onOpenSettings: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.nav_statistics),
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            TextButton(onClick = onShowRules) {
                Text(
                    text = stringResource(R.string.statistics_rules_short),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(onClick = onOpenSettings) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        windowInsets = WindowInsets(0)
    )
}

@Composable
private fun StatisticsPeriodSelector(
    period: StatisticsPeriod,
    onPeriodChange: (StatisticsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatisticsPeriod.entries.forEach { item ->
            FilterChip(
                selected = item == period,
                onClick = { onPeriodChange(item) },
                label = { Text(text = stringResource(periodLabelRes(item))) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@StringRes
private fun periodLabelRes(period: StatisticsPeriod): Int = when (period) {
    StatisticsPeriod.WEEK -> R.string.statistics_period_week
    StatisticsPeriod.MONTH -> R.string.statistics_period_month
    StatisticsPeriod.ALL -> R.string.statistics_period_all
}

@Composable
private fun StatisticsError(onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            iconRes = R.drawable.ic_empty_habits,
            title = stringResource(R.string.statistics_load_error_title),
            description = stringResource(R.string.statistics_load_error_description),
            actionLabel = stringResource(R.string.action_retry),
            onAction = onRetry
        )
    }
}

@Composable
private fun StatisticsEmpty(onCreateHabit: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            iconRes = R.drawable.ic_empty_habits,
            title = stringResource(R.string.statistics_empty_title),
            description = stringResource(R.string.statistics_empty_description),
            actionLabel = stringResource(R.string.action_create_habit),
            onAction = onCreateHabit
        )
    }
}

@Composable
private fun StatisticsSkeleton() {
    val placeholder = MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(placeholder)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(placeholder)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(placeholder)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(placeholder)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(placeholder)
        )
    }
}

@Composable
private fun StatisticsContent(summary: StatisticsSummary, rangeLabel: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PercentCard(summary = summary, rangeLabel = rangeLabel) }
        item { TotalsCards(summary = summary) }
        item { StreakCard(summary = summary) }
        item { BestHabitCard(habitStatistics = summary.bestHabit) }
        item { WeekdayChartCard(summary = summary) }
        item { DoneSkippedCard(summary = summary) }
        item { RatingCard(summary = summary) }
    }
}

@Composable
private fun PercentCard(summary: StatisticsSummary, rangeLabel: String) {
    val totals = summary.totals
    val percentText = totals.percent
        ?.let { stringResource(R.string.statistics_percent_value, it) }
        ?: stringResource(R.string.statistics_no_value)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = rangeLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = percentText,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.statistics_percent_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            if (totals.expected > 0) {
                LinearProgressIndicator(
                    progress = { totals.done.toFloat() / totals.expected },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Text(
                    text = stringResource(
                        R.string.statistics_percent_caption,
                        totals.done,
                        totals.expected
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = stringResource(R.string.statistics_no_expected),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (totals.done == 0) {
                Text(
                    text = stringResource(R.string.statistics_done_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TotalsCards(summary: StatisticsSummary) {
    val totals = summary.totals
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            title = stringResource(R.string.statistics_done_label),
            value = totals.done.toString(),
            supporting = stringResource(R.string.statistics_metric_expected, totals.expected),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = stringResource(R.string.statistics_skipped_label),
            value = totals.skipped.toString(),
            supporting = stringResource(R.string.statistics_pending_note, totals.pending),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    supporting: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StreakCard(summary: StatisticsSummary) {
    val best = summary.bestMaxStreakHabit
    val current = summary.bestCurrentStreakHabit
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.statistics_streak_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            if (best == null) {
                Text(
                    text = stringResource(R.string.statistics_streak_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = stringResource(R.string.statistics_streak_value, best.maxStreak),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.statistics_streak_days),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = best.habit.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (current != null) {
                    Text(
                        text = stringResource(
                            R.string.statistics_current_streak,
                            current.currentStreak,
                            current.habit.name
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun BestHabitCard(habitStatistics: HabitPeriodStatistics?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.statistics_best_habit_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (habitStatistics == null) {
                Text(
                    text = stringResource(R.string.statistics_best_habit_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HabitBadge(
                        icon = habitStatistics.habit.icon,
                        color = habitStatistics.habit.color
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habitStatistics.habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(
                                R.string.statistics_best_habit_detail,
                                habitStatistics.done,
                                habitStatistics.expected,
                                habitStatistics.percentOrNull
                                    ?.let { stringResource(R.string.statistics_percent_value, it) }
                                    ?: stringResource(R.string.statistics_no_value)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitBadge(icon: String, color: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(habitContainerColor(color)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = HabitIcons.drawableRes(icon)),
            contentDescription = null,
            tint = habitColor(color),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ChartCard(
    title: String,
    caption: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (caption != null) {
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun WeekdayChartCard(summary: StatisticsSummary) {
    ChartCard(
        title = stringResource(R.string.statistics_weekday_title),
        caption = stringResource(R.string.statistics_weekday_caption)
    ) {
        WeekdayBarChart(summary.weekdays)
    }
}

@Composable
private fun WeekdayBarChart(weekdays: List<WeekdayDone>) {
    val labels = stringArrayResource(R.array.week_days_short)
    val maxDone = weekdays.maxOfOrNull { it.done } ?: 0
    val barColor = MaterialTheme.colorScheme.primary
    val emptyBarColor = MaterialTheme.colorScheme.surfaceVariant
    val valueColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        weekdays.forEachIndexed { index, item ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.done.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = valueColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BarAreaHeight),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(weekdayBarHeight(item.done, maxDone))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (item.done > 0) barColor else emptyBarColor)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = labels.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = valueColor
                )
            }
        }
    }
}

private fun weekdayBarHeight(done: Int, maxDone: Int): Dp {
    if (done <= 0 || maxDone <= 0) return 2.dp
    return (BarAreaHeight * (done.toFloat() / maxDone)).coerceAtLeast(6.dp)
}

@Composable
private fun DoneSkippedCard(summary: StatisticsSummary) {
    ChartCard(
        title = stringResource(R.string.statistics_pie_title),
        caption = stringResource(R.string.statistics_pie_caption)
    ) {
        if (!summary.hasExplicitCompletions) {
            Text(
                text = stringResource(R.string.statistics_pie_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                DoneSkippedPie(
                    done = summary.totals.done,
                    skipped = summary.totals.skipped,
                    modifier = Modifier.size(116.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LegendItem(
                        color = MaterialTheme.colorScheme.primary,
                        text = stringResource(
                            R.string.statistics_pie_done,
                            summary.totals.done,
                            summary.doneSharePercent
                        )
                    )
                    LegendItem(
                        color = MaterialTheme.colorScheme.tertiary,
                        text = stringResource(
                            R.string.statistics_pie_skipped,
                            summary.totals.skipped,
                            summary.skippedSharePercent
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DoneSkippedPie(done: Int, skipped: Int, modifier: Modifier = Modifier) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val doneColor = MaterialTheme.colorScheme.primary
    val skippedColor = MaterialTheme.colorScheme.tertiary
    val description = stringResource(R.string.statistics_pie_description, done, skipped)
    Canvas(modifier = modifier.semantics { contentDescription = description }) {
        val strokeWidth = size.minDimension * 0.18f
        val inset = strokeWidth / 2f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth)
        )
        val total = done + skipped
        if (total > 0) {
            val doneSweep = 360f * done / total
            if (doneSweep > 0f) {
                drawArc(
                    color = doneColor,
                    startAngle = -90f,
                    sweepAngle = doneSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
            }
            if (doneSweep < 360f) {
                drawArc(
                    color = skippedColor,
                    startAngle = -90f + doneSweep,
                    sweepAngle = 360f - doneSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun RatingCard(summary: StatisticsSummary) {
    ChartCard(
        title = stringResource(R.string.statistics_rating_title),
        caption = stringResource(R.string.statistics_rating_caption)
    ) {
        if (summary.rating.isEmpty()) {
            Text(
                text = stringResource(R.string.statistics_rating_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                summary.rating.forEach { habitStatistics ->
                    RatingRow(habitStatistics = habitStatistics)
                }
            }
        }
    }
}

@Composable
private fun RatingRow(habitStatistics: HabitPeriodStatistics) {
    val percentText = habitStatistics.percentOrNull
        ?.let { stringResource(R.string.statistics_percent_value, it) }
        ?: stringResource(R.string.statistics_no_value)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HabitBadge(icon = habitStatistics.habit.icon, color = habitStatistics.habit.color)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = habitStatistics.habit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = percentText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            LinearProgressIndicator(
                progress = { habitStatistics.percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = habitColor(habitStatistics.habit.color),
                trackColor = habitContainerColor(habitStatistics.habit.color)
            )
            Text(
                text = stringResource(
                    R.string.statistics_rating_detail,
                    habitStatistics.done,
                    habitStatistics.expected,
                    habitStatistics.currentStreak,
                    habitStatistics.maxStreak
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatisticsRulesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.statistics_rules_title)) },
        text = { Text(text = stringResource(R.string.statistics_rules_text)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_close))
            }
        }
    )
}
