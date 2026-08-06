package dev.whole30journal.feature.home.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.components.DSCard
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.core.designsystem.theme.scoreColor
import dev.whole30journal.feature.home.presentation.generated.resources.Res
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_cravings
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_energy
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_overall
import dev.whole30journal.feature.home.presentation.generated.resources.home_metric_sleep
import dev.whole30journal.feature.home.presentation.generated.resources.home_trends_title
import dev.whole30journal.feature.home.presentation.vm.HomeContract
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun TrendsSection(
    selectedMetric: HomeContract.TrendMetric,
    series: List<HomeContract.TrendPoint>,
    totalDays: Int,
    trendAxisLabels: HomeContract.TrendAxisLabels,
    onMetricSelect: (HomeContract.TrendMetric) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
        Text(
            text = stringResource(Res.string.home_trends_title),
            style = DSTheme.typography.textXl,
            color = DSTheme.colors.text,
        )
        DSCard(modifier = Modifier.fillMaxWidth()) {
            TrendMetricSelector(selected = selectedMetric, onSelect = onMetricSelect)
            TrendBarChart(
                series = series,
                totalDays = totalDays,
                axisLabels = trendAxisLabels,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TrendMetricSelector(
    selected: HomeContract.TrendMetric,
    onSelect: (HomeContract.TrendMetric) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    val entries = HomeContract.TrendMetric.entries
    val selectedIndex = entries.indexOf(selected)
    val animatedSelectedIndex by animateFloatAsState(targetValue = selectedIndex.toFloat(), label = "trendMetricIndicator")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(DSShapes.pill)
            .background(colors.surface2)
            .padding(DSSpacing.space1)
            .drawBehind {
                val segmentWidth = size.width / entries.size
                drawRoundRect(
                    color = colors.accent,
                    topLeft = Offset(x = segmentWidth * animatedSelectedIndex, y = 0f),
                    size = Size(segmentWidth, size.height),
                    cornerRadius = CornerRadius(size.height / 2f),
                )
            },
    ) {
        entries.forEach { metric ->
            val isSelected = metric == selected
            val segmentFontSize = DSTheme.typography.textXs.fontSize
            val textStyle = if (isSelected) {
                DSTheme.typography.textSm.copy(fontSize = segmentFontSize, fontWeight = FontWeight.Bold)
            } else {
                DSTheme.typography.textSm.copy(fontSize = segmentFontSize)
            }
            val textColor by animateColorAsState(
                targetValue = if (isSelected) colors.accentOn else colors.textSecondary,
                label = "trendMetricTextColor",
            )
            Text(
                text = trendMetricLabel(metric),
                style = textStyle,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .clip(DSShapes.pill)
                    .clickable { onSelect(metric) }
                    .padding(vertical = DSSpacing.space3),
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
private fun TrendBarChart(
    series: List<HomeContract.TrendPoint>,
    totalDays: Int,
    axisLabels: HomeContract.TrendAxisLabels,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = DSTheme.typography.text2xs.copy(color = colors.textTertiary)
    val maxValue = 10f
    val animatedSeries = series.map { point ->
        point.dayNumber to key(point.dayNumber) {
            animateFloatAsState(targetValue = point.value.toFloat(), label = "trendBarValue")
        }
    }

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
            val slotWidth = size.width / totalDays
            val barWidth = slotWidth * 0.6f
            animatedSeries.forEach { (dayNumber, animatedValue) ->
                val value = animatedValue.value
                val barHeight = (value / maxValue) * size.height
                val left = (dayNumber - 1) * slotWidth + (slotWidth - barWidth) / 2f
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
                drawPath(path = barPath, color = colors.scoreColor(value.roundToInt()))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = axisLabels.start, style = DSTheme.typography.text2xs, color = colors.textTertiary)
            Text(text = axisLabels.middle, style = DSTheme.typography.text2xs, color = colors.textTertiary)
            Text(text = axisLabels.end, style = DSTheme.typography.text2xs, color = colors.textTertiary)
        }
    }
}

private fun previewSeries(): List<HomeContract.TrendPoint> {
    val trendValues = listOf(3, 3, 5, 4, 5, 5, 6, 7, 8, 8, 8, 8)
    return trendValues.mapIndexed { index, value -> HomeContract.TrendPoint(dayNumber = index + 1, value = value) }
}

@Preview
@Composable
private fun TrendsSectionPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            TrendsSection(
                selectedMetric = HomeContract.TrendMetric.Overall,
                series = previewSeries(),
                totalDays = 12,
                trendAxisLabels = HomeContract.TrendAxisLabels(start = "25.7.2026", middle = "8.8.2026", end = "23.8.2026"),
                onMetricSelect = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun TrendsSectionPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            TrendsSection(
                selectedMetric = HomeContract.TrendMetric.Overall,
                series = previewSeries(),
                totalDays = 12,
                trendAxisLabels = HomeContract.TrendAxisLabels(start = "25.7.2026", middle = "8.8.2026", end = "23.8.2026"),
                onMetricSelect = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
