package dev.whole30journal.feature.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.components.Whole30Card
import dev.whole30journal.core.designsystem.components.Whole30ProgressRing
import dev.whole30journal.core.designsystem.theme.Whole30Shapes
import dev.whole30journal.core.designsystem.theme.Whole30Spacing
import dev.whole30journal.core.designsystem.theme.Whole30Theme
import dev.whole30journal.feature.home.presentation.generated.resources.Res
import dev.whole30journal.feature.home.presentation.generated.resources.home_day_future_message
import dev.whole30journal.feature.home.presentation.generated.resources.home_day_no_entry_message
import dev.whole30journal.feature.home.presentation.generated.resources.home_edit_today_content_description
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_cravings
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_energy
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_mood
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_overall
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_sleep
import dev.whole30journal.feature.home.presentation.generated.resources.home_view_today_details_content_description
import dev.whole30journal.feature.home.presentation.ui.icons.CravingsIcon
import dev.whole30journal.feature.home.presentation.ui.icons.EditIcon
import dev.whole30journal.feature.home.presentation.ui.icons.EnergyIcon
import dev.whole30journal.feature.home.presentation.ui.icons.MoodIcon
import dev.whole30journal.feature.home.presentation.ui.icons.SleepIcon
import dev.whole30journal.feature.home.presentation.ui.icons.ViewDetailsIcon
import dev.whole30journal.feature.home.presentation.vm.HomeContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun DayOverviewCard(
    selectedDay: Int,
    selectedDayLabel: String,
    currentDay: Int,
    totalDays: Int,
    metrics: HomeContract.DayMetrics?,
    onEditClick: () -> Unit,
    onViewDetailsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Whole30Theme.colors
    val isFuture = selectedDay > currentDay
    Whole30Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Whole30Spacing.space3),
            ) {
                Text(
                    text = selectedDayLabel,
                    style = Whole30Theme.typography.textBase.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary,
                )
                Text(
                    text = "$selectedDay/$totalDays",
                    style = Whole30Theme.typography.text2xs,
                    color = colors.accentOn,
                    modifier = Modifier
                        .clip(Whole30Shapes.pill)
                        .background(colors.accent)
                        .padding(horizontal = Whole30Spacing.space3, vertical = Whole30Spacing.space1),
                )
            }
            if (!isFuture) {
                Row(horizontalArrangement = Arrangement.spacedBy(Whole30Spacing.space3)) {
                    IconCircleButton(onClick = onEditClick) {
                        EditIcon(
                            tint = colors.text,
                            modifier = Modifier.size(14.dp),
                            contentDescription = stringResource(Res.string.home_edit_today_content_description),
                        )
                    }
                    if (metrics != null) {
                        IconCircleButton(onClick = onViewDetailsClick) {
                            ViewDetailsIcon(
                                tint = colors.text,
                                modifier = Modifier.size(14.dp),
                                contentDescription = stringResource(Res.string.home_view_today_details_content_description),
                            )
                        }
                    }
                }
            }
        }
        if (metrics != null) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val ringSize = 150.dp
                val gridWidth = (maxWidth - ringSize - Whole30Spacing.space7).coerceAtLeast(0.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Whole30Spacing.space7),
                ) {
                    Whole30ProgressRing(
                        score = metrics.overall,
                        size = ringSize,
                        label = stringResource(Res.string.home_metric_overall),
                    )
                    MetricGrid(metrics = metrics, modifier = Modifier.width(gridWidth))
                }
            }
        } else {
            DayEmptyState(isFuture = isFuture)
        }
    }
}

@Composable
private fun DayEmptyState(isFuture: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = if (isFuture) {
            stringResource(Res.string.home_day_future_message)
        } else {
            stringResource(Res.string.home_day_no_entry_message)
        },
        style = Whole30Theme.typography.textSm,
        color = Whole30Theme.colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Whole30Spacing.space8),
    )
}

@Composable
private fun IconCircleButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = Whole30Theme.colors
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(28.dp)
            .clip(CircleShape)
            .border(width = 1.dp, color = colors.divider, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun MetricGrid(metrics: HomeContract.DayMetrics, modifier: Modifier = Modifier) {
    val colors = Whole30Theme.colors

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Whole30Spacing.space5)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCell(
                label = stringResource(Res.string.home_metric_energy),
                value = metrics.energy,
                modifier = Modifier.fillMaxWidth(HALF_WIDTH_FRACTION).padding(end = Whole30Spacing.space6),
            ) {
                EnergyIcon(tint = colors.iconEnergy, modifier = Modifier.size(16.dp))
            }
            MetricCell(
                label = stringResource(Res.string.home_metric_mood),
                value = metrics.mood,
                modifier = Modifier.fillMaxWidth(),
            ) {
                MoodIcon(tint = colors.iconMood, modifier = Modifier.size(16.dp))
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCell(
                label = stringResource(Res.string.home_metric_sleep),
                value = metrics.sleep,
                modifier = Modifier.fillMaxWidth(HALF_WIDTH_FRACTION).padding(end = Whole30Spacing.space6),
            ) {
                SleepIcon(tint = colors.iconSleep, modifier = Modifier.size(16.dp))
            }
            // Cravings uses colors.accent directly per the design spec (not colors.iconCravings) -
            // the two are numerically identical in both themes today, but accent is what was chosen.
            MetricCell(
                label = stringResource(Res.string.home_metric_cravings),
                value = metrics.cravings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                CravingsIcon(tint = colors.accent, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private const val HALF_WIDTH_FRACTION = 0.5f

@Composable
private fun MetricCell(label: String, value: Int?, modifier: Modifier = Modifier, icon: @Composable () -> Unit) {
    val colors = Whole30Theme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Whole30Spacing.space3),
    ) {
        icon()
        Column {
            Text(text = label, style = Whole30Theme.typography.textXs, color = colors.textSecondary)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value?.toString() ?: "–", style = Whole30Theme.typography.textLg, color = colors.text)
                Text(text = "/10", style = Whole30Theme.typography.textXs, color = colors.textTertiary)
            }
        }
    }
}

private fun previewMetrics() = HomeContract.DayMetrics(overall = 4, energy = 3, mood = 4, sleep = 5, cravings = 3)

@Preview
@Composable
private fun DayOverviewCardPreviewLight() {
    Whole30Theme(darkTheme = false) {
        Surface(color = Whole30Theme.colors.bg) {
            DayOverviewCard(
                selectedDay = 2,
                selectedDayLabel = "Sunday 26.7.2026",
                currentDay = 12,
                totalDays = 30,
                metrics = previewMetrics(),
                onEditClick = {},
                onViewDetailsClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun DayOverviewCardPreviewDark() {
    Whole30Theme(darkTheme = true) {
        Surface(color = Whole30Theme.colors.bg) {
            DayOverviewCard(
                selectedDay = 2,
                selectedDayLabel = "Sunday 26.7.2026",
                currentDay = 12,
                totalDays = 30,
                metrics = previewMetrics(),
                onEditClick = {},
                onViewDetailsClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun DayOverviewCardEmptyPreviewLight() {
    Whole30Theme(darkTheme = false) {
        Surface(color = Whole30Theme.colors.bg) {
            DayOverviewCard(
                selectedDay = 5,
                selectedDayLabel = "Wednesday 29.7.2026",
                currentDay = 12,
                totalDays = 30,
                metrics = null,
                onEditClick = {},
                onViewDetailsClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun DayOverviewCardEmptyPreviewDark() {
    Whole30Theme(darkTheme = true) {
        Surface(color = Whole30Theme.colors.bg) {
            DayOverviewCard(
                selectedDay = 5,
                selectedDayLabel = "Wednesday 29.7.2026",
                currentDay = 12,
                totalDays = 30,
                metrics = null,
                onEditClick = {},
                onViewDetailsClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
