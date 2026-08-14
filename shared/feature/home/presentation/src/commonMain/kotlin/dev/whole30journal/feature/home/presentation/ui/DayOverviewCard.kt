package dev.whole30journal.feature.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import dev.whole30journal.core.designsystem.components.DSCard
import dev.whole30journal.core.designsystem.components.DSProgressRing
import dev.whole30journal.core.designsystem.components.DSTag
import dev.whole30journal.core.designsystem.components.DSTagTone
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.home.presentation.generated.resources.Res
import dev.whole30journal.feature.home.presentation.generated.resources.home_day_complete_tag
import dev.whole30journal.feature.home.presentation.generated.resources.home_day_future_message
import dev.whole30journal.feature.home.presentation.generated.resources.home_day_no_entry_message
import dev.whole30journal.feature.home.presentation.generated.resources.home_day_not_complete_tag
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
import dev.whole30journal.feature.home.presentation.ui.icons.LeafIcon
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
    val colors = DSTheme.colors
    val isFuture = selectedDay > currentDay
    DSCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DSSpacing.space3),
            ) {
                Text(
                    text = selectedDayLabel,
                    style = DSTheme.typography.textBase.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary,
                )
                Text(
                    text = "$selectedDay/$totalDays",
                    style = DSTheme.typography.text2xs,
                    color = colors.accentOn,
                    modifier = Modifier
                        .clip(DSShapes.pill)
                        .background(colors.accent)
                        .padding(horizontal = DSSpacing.space3, vertical = DSSpacing.space1),
                )
            }
            if (!isFuture) {
                Row(horizontalArrangement = Arrangement.spacedBy(DSSpacing.space3)) {
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
            DSTag(
                text = if (metrics.isComplete) {
                    stringResource(Res.string.home_day_complete_tag)
                } else {
                    stringResource(Res.string.home_day_not_complete_tag)
                },
                tone = if (metrics.isComplete) DSTagTone.Accent else DSTagTone.Neutral,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DSSpacing.space7),
            ) {
                DSProgressRing(
                    score = metrics.overall,
                    size = OVERALL_RING_SIZE,
                    stroke = OVERALL_RING_STROKE,
                    label = stringResource(Res.string.home_metric_overall),
                    icon = { LeafIcon(tint = colors.accent, modifier = Modifier.size(METRIC_ICON_SIZE)) },
                )
                MetricGrid(metrics = metrics, modifier = Modifier.weight(1f))
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
        style = DSTheme.typography.textSm,
        color = DSTheme.colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = DSSpacing.space8),
    )
}

@Composable
private fun IconCircleButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = DSTheme.colors
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
    val colors = DSTheme.colors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(DSSpacing.space5),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(DSSpacing.space10)) {
            DSProgressRing(
                score = metrics.energy,
                size = METRIC_RING_SIZE,
                stroke = METRIC_RING_STROKE,
                label = stringResource(Res.string.home_metric_energy),
                icon = { EnergyIcon(tint = colors.iconEnergy, modifier = Modifier.size(METRIC_ICON_SIZE)) },
            )
            DSProgressRing(
                score = metrics.mood,
                size = METRIC_RING_SIZE,
                stroke = METRIC_RING_STROKE,
                label = stringResource(Res.string.home_metric_mood),
                icon = { MoodIcon(tint = colors.iconMood, modifier = Modifier.size(METRIC_ICON_SIZE)) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(DSSpacing.space10)) {
            DSProgressRing(
                score = metrics.sleep,
                size = METRIC_RING_SIZE,
                stroke = METRIC_RING_STROKE,
                label = stringResource(Res.string.home_metric_sleep),
                icon = { SleepIcon(tint = colors.iconSleep, modifier = Modifier.size(METRIC_ICON_SIZE)) },
            )
            DSProgressRing(
                score = metrics.cravings,
                size = METRIC_RING_SIZE,
                stroke = METRIC_RING_STROKE,
                label = stringResource(Res.string.home_metric_cravings),
                icon = { CravingsIcon(tint = colors.accent, modifier = Modifier.size(METRIC_ICON_SIZE)) },
            )
        }
    }
}

private val OVERALL_RING_SIZE = 96.dp
private val OVERALL_RING_STROKE = 6.dp
private val METRIC_RING_SIZE = 64.dp
private val METRIC_RING_STROKE = 4.dp
private val METRIC_ICON_SIZE = 12.dp

private fun previewMetrics() =
    HomeContract.DayMetrics(isComplete = true, overall = 4, energy = 3, mood = 4, sleep = 5, cravings = 3)

@Preview
@Composable
private fun DayOverviewCardPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
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
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
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
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
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
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
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
