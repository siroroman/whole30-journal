package dev.whole30journal.feature.home.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.components.Whole30Card
import dev.whole30journal.core.designsystem.components.Whole30ProgressBar
import dev.whole30journal.core.designsystem.components.Whole30ProgressRing
import dev.whole30journal.core.designsystem.theme.Whole30Shapes
import dev.whole30journal.core.designsystem.theme.Whole30Spacing
import dev.whole30journal.core.designsystem.theme.Whole30Theme
import dev.whole30journal.core.designsystem.theme.scoreColor
import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.feature.home.presentation.generated.resources.Res
import dev.whole30journal.feature.home.presentation.generated.resources.home_edit_today_content_description
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_cravings
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_energy
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_mood
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_overall
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_sleep
import dev.whole30journal.feature.home.presentation.generated.resources.home_today_label
import dev.whole30journal.feature.home.presentation.generated.resources.home_trends_title
import dev.whole30journal.feature.home.presentation.generated.resources.home_view_today_details_content_description
import dev.whole30journal.feature.home.presentation.generated.resources.home_wordmark
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
fun HomeScreen(
    state: UiStateAware.UiState<HomeContract.UiData, HomeContract.UiEvent>,
    onUiAction: (HomeContract.UiAction) -> Unit,
    onUiEventConsume: (HomeContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Whole30Theme {
        Scaffold(modifier = modifier, containerColor = Whole30Theme.colors.bg) { contentPadding ->
            HomeContent(
                uiData = state.uiData,
                onUiAction = onUiAction,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize(),
            )
            HandleUiEvents(events = state.uiEvents, onConsume = onUiEventConsume)
        }
    }
}

@Composable
private fun HandleUiEvents(events: List<HomeContract.UiEvent>, onConsume: (HomeContract.UiEvent) -> Unit) {
    // HomeContract.UiEvent has no cases yet, so this never actually fires - kept for signature
    // parity with the Android/iOS call sites until a real transient event exists.
    events.forEach { event ->
        LaunchedEffect(event) { onConsume(event) }
    }
}

@Composable
private fun HomeContent(
    uiData: HomeContract.UiData,
    onUiAction: (HomeContract.UiAction) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = Whole30Spacing.space7, vertical = Whole30Spacing.space7),
        verticalArrangement = Arrangement.spacedBy(Whole30Spacing.space8),
    ) {
        HomeHeader(currentDay = uiData.currentDay, totalDays = uiData.totalDays)
        Whole30ProgressBar(
            value = uiData.currentDay,
            modifier = Modifier.fillMaxWidth(),
            max = uiData.totalDays,
            labelLeft = "Day 1",
            labelCenter = "Day ${uiData.currentDay} · ${uiData.progressPercent}%",
            labelRight = "Day ${uiData.totalDays}",
        )
        DayStrip(days = uiData.days, onDayClick = { onUiAction(HomeContract.UiAction.OnDayClick(it)) })
        TodayCard(
            today = uiData.today,
            onEditClick = { onUiAction(HomeContract.UiAction.OnEditTodayClick) },
            onViewDetailsClick = { onUiAction(HomeContract.UiAction.OnViewTodayDetailsClick) },
        )
        TrendsSection(
            selectedMetric = uiData.selectedTrendMetric,
            series = uiData.trendSeries[uiData.selectedTrendMetric].orEmpty(),
            totalDays = uiData.totalDays,
            onMetricSelect = { onUiAction(HomeContract.UiAction.OnTrendMetricSelected(it)) },
        )
    }
}

@Composable
private fun HomeHeader(currentDay: Int, totalDays: Int, modifier: Modifier = Modifier) {
    val colors = Whole30Theme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Whole30Spacing.space4),
        ) {
            Box(
                modifier = Modifier.size(28.dp).clip(Whole30Shapes.sm).background(colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                LeafIcon(tint = colors.accentOn, modifier = Modifier.size(16.dp))
            }
            Text(text = stringResource(Res.string.home_wordmark), style = Whole30Theme.typography.textXl, color = colors.text)
        }
        Row(
            modifier = Modifier
                .clip(Whole30Shapes.pill)
                .background(colors.surface)
                .padding(horizontal = Whole30Spacing.space5, vertical = Whole30Spacing.space3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$currentDay",
                style = Whole30Theme.typography.textBase.copy(fontWeight = FontWeight.Bold),
                color = colors.accent,
            )
            Text(text = " / $totalDays", style = Whole30Theme.typography.textSm, color = colors.textSecondary)
        }
    }
}

@Composable
private fun DayStrip(days: List<HomeContract.DayCell>, onDayClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Whole30Spacing.space3),
    ) {
        items(items = days, key = { it.dayNumber }) { day ->
            DayCellItem(day = day, onClick = { onDayClick(day.dayNumber) })
        }
    }
}

@Composable
private fun DayCellItem(day: HomeContract.DayCell, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Whole30Theme.colors
    val backgroundColor = if (day.isToday) colors.accentTint else Color.Transparent
    val (weekdayColor, numberColor) = when {
        day.isToday -> colors.accent to colors.accent
        day.isFilled -> colors.textSecondary to colors.text
        else -> colors.textTertiary to colors.textTertiary
    }
    Column(
        modifier = modifier
            .width(46.dp)
            .clip(Whole30Shapes.lg)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = Whole30Spacing.space3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Whole30Spacing.space1),
    ) {
        Text(text = day.weekdayAbbreviation, style = Whole30Theme.typography.text2xs, color = weekdayColor)
        Text(text = "${day.dayNumber}", style = Whole30Theme.typography.textXl, color = numberColor)
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (day.isFilled) colors.accent else Color.Transparent),
        )
    }
}

@Composable
private fun TodayCard(
    today: HomeContract.TodayMetrics,
    onEditClick: () -> Unit,
    onViewDetailsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Whole30Theme.colors
    Whole30Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.home_today_label),
                style = Whole30Theme.typography.textBase.copy(fontWeight = FontWeight.Bold),
                color = colors.textSecondary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Whole30Spacing.space3)) {
                IconCircleButton(onClick = onEditClick) {
                    EditIcon(
                        tint = colors.text,
                        modifier = Modifier.size(14.dp),
                        contentDescription = stringResource(Res.string.home_edit_today_content_description),
                    )
                }
                IconCircleButton(onClick = onViewDetailsClick) {
                    ViewDetailsIcon(
                        tint = colors.text,
                        modifier = Modifier.size(14.dp),
                        contentDescription = stringResource(Res.string.home_view_today_details_content_description),
                    )
                }
            }
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val ringSize = 150.dp
            val gridWidth = (maxWidth - ringSize - Whole30Spacing.space7).coerceAtLeast(0.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Whole30Spacing.space7),
            ) {
                Whole30ProgressRing(
                    score = today.overall,
                    size = ringSize,
                    label = stringResource(Res.string.home_metric_overall),
                )
                MetricGrid(today = today, modifier = Modifier.width(gridWidth))
            }
        }
    }
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
private fun MetricGrid(today: HomeContract.TodayMetrics, modifier: Modifier = Modifier) {
    val colors = Whole30Theme.colors

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Whole30Spacing.space5)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCell(
                label = stringResource(Res.string.home_metric_energy),
                value = today.energy,
                modifier = Modifier.fillMaxWidth(HALF_WIDTH_FRACTION).padding(end = Whole30Spacing.space6),
            ) {
                EnergyIcon(tint = colors.iconEnergy, modifier = Modifier.size(16.dp))
            }
            MetricCell(
                label = stringResource(Res.string.home_metric_mood),
                value = today.mood,
                modifier = Modifier.fillMaxWidth(),
            ) {
                MoodIcon(tint = colors.iconMood, modifier = Modifier.size(16.dp))
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCell(
                label = stringResource(Res.string.home_metric_sleep),
                value = today.sleep,
                modifier = Modifier.fillMaxWidth(HALF_WIDTH_FRACTION).padding(end = Whole30Spacing.space6),
            ) {
                SleepIcon(tint = colors.iconSleep, modifier = Modifier.size(16.dp))
            }
            // Cravings uses colors.accent directly per the design spec (not colors.iconCravings) -
            // the two are numerically identical in both themes today, but accent is what was chosen.
            MetricCell(
                label = stringResource(Res.string.home_metric_cravings),
                value = today.cravings,
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

@Composable
private fun TrendsSection(
    selectedMetric: HomeContract.TrendMetric,
    series: List<HomeContract.TrendPoint>,
    totalDays: Int,
    onMetricSelect: (HomeContract.TrendMetric) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Whole30Spacing.space5)) {
        Text(
            text = stringResource(Res.string.home_trends_title),
            style = Whole30Theme.typography.textXl,
            color = Whole30Theme.colors.text,
        )
        Whole30Card(modifier = Modifier.fillMaxWidth()) {
            TrendMetricSelector(selected = selectedMetric, onSelect = onMetricSelect)
            TrendBarChart(series = series, totalDays = totalDays, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun TrendMetricSelector(
    selected: HomeContract.TrendMetric,
    onSelect: (HomeContract.TrendMetric) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Whole30Theme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Whole30Shapes.pill)
            .background(colors.surface2)
            .padding(Whole30Spacing.space1),
    ) {
        val entries = HomeContract.TrendMetric.entries
        // fillMaxWidth(1/n) instead of weight(1f) - see the HALF_WIDTH_FRACTION note above; this
        // Row has no Arrangement.spacedBy gap, so equal fractions divide it exactly.
        val segmentWidthFraction = 1f / entries.size
        entries.forEach { metric ->
            val isSelected = metric == selected
            val segmentFontSize = Whole30Theme.typography.textXs.fontSize
            val textStyle = if (isSelected) {
                Whole30Theme.typography.textSm.copy(fontSize = segmentFontSize, fontWeight = FontWeight.Bold)
            } else {
                Whole30Theme.typography.textSm.copy(fontSize = segmentFontSize)
            }
            val textColor = if (isSelected) colors.text else colors.textSecondary
            val chipModifier = if (isSelected) Modifier.background(colors.surface) else Modifier
            Text(
                text = trendMetricLabel(metric),
                style = textStyle,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth(segmentWidthFraction)
                    .clip(Whole30Shapes.pill)
                    .then(chipModifier)
                    .clickable { onSelect(metric) }
                    .padding(vertical = Whole30Spacing.space3),
            )
        }
    }
}

@Composable
private fun trendMetricLabel(metric: HomeContract.TrendMetric): String = when (metric) {
    HomeContract.TrendMetric.Overall -> stringResource(Res.string.home_metric_overall)
    HomeContract.TrendMetric.Energy -> stringResource(Res.string.home_metric_energy)
    HomeContract.TrendMetric.Sleep -> stringResource(Res.string.home_metric_sleep)
    HomeContract.TrendMetric.Cravings -> stringResource(Res.string.home_metric_cravings)
}

@Composable
private fun TrendBarChart(series: List<HomeContract.TrendPoint>, totalDays: Int, modifier: Modifier = Modifier) {
    val colors = Whole30Theme.colors
    val textMeasurer = rememberTextMeasurer()
    val average = remember(series) { if (series.isEmpty()) 0f else series.map { it.value }.average().toFloat() }
    val labelStyle = Whole30Theme.typography.text2xs.copy(color = colors.textTertiary)
    val maxValue = 10f

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            listOf(0, 5, 10).forEach { gridValue ->
                val y = size.height - (gridValue / maxValue) * size.height
                drawLine(
                    color = colors.gridLine,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "$gridValue",
                    topLeft = Offset(0f, (y - 12.dp.toPx()).coerceAtLeast(0f)),
                    style = labelStyle,
                )
            }
            val avgY = size.height - (average / maxValue) * size.height
            drawLine(
                color = colors.avgLine,
                start = Offset(0f, avgY),
                end = Offset(size.width, avgY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
            )
            val slotWidth = size.width / totalDays
            val barWidth = slotWidth * 0.6f
            series.forEach { point ->
                val barHeight = (point.value / maxValue) * size.height
                val left = (point.dayNumber - 1) * slotWidth + (slotWidth - barWidth) / 2f
                val topCorner = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                val barPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = left,
                            top = size.height - barHeight,
                            right = left + barWidth,
                            bottom = size.height,
                            topLeftCornerRadius = topCorner,
                            topRightCornerRadius = topCorner,
                            bottomLeftCornerRadius = CornerRadius.Zero,
                            bottomRightCornerRadius = CornerRadius.Zero,
                        ),
                    )
                }
                drawPath(path = barPath, color = colors.scoreColor(point.value))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Day 1", style = Whole30Theme.typography.text2xs, color = colors.textTertiary)
            Text(text = "Day 15", style = Whole30Theme.typography.text2xs, color = colors.textTertiary)
            Text(text = "Day $totalDays", style = Whole30Theme.typography.text2xs, color = colors.textTertiary)
        }
    }
}

private fun previewUiData(): HomeContract.UiData {
    val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
    val currentDay = 12
    val totalDays = 30
    val trendValues = listOf(6, 7, 5, 8, 7, 9, 6, 8, 7, 9, 8, 9)
    return HomeContract.UiData(
        currentDay = currentDay,
        totalDays = totalDays,
        progressPercent = 40,
        days = (1..totalDays).map { day ->
            HomeContract.DayCell(
                dayNumber = day,
                weekdayAbbreviation = weekdays[(day - 1) % weekdays.size],
                isFilled = day <= currentDay,
                isToday = day == currentDay,
            )
        },
        today = HomeContract.TodayMetrics(overall = 8, energy = 7, mood = 8, sleep = 6, cravings = 3),
        selectedTrendMetric = HomeContract.TrendMetric.Overall,
        trendSeries = HomeContract.TrendMetric.entries.associateWith { metric ->
            trendValues.mapIndexed { index, value -> HomeContract.TrendPoint(dayNumber = index + 1, value = value) }
        },
    )
}

private const val UI_MODE_NIGHT_YES = 0x20

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        state = UiStateAware.UiState(isLoading = false, uiData = previewUiData()),
        onUiAction = {},
        onUiEventConsume = {},
    )
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreviewDark() {
    HomeScreen(
        state = UiStateAware.UiState(isLoading = false, uiData = previewUiData()),
        onUiAction = {},
        onUiEventConsume = {},
    )
}
