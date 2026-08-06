package dev.whole30journal.feature.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.home.presentation.generated.resources.Res
import dev.whole30journal.feature.home.presentation.generated.resources.home_days_title
import dev.whole30journal.feature.home.presentation.vm.HomeContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun DayStrip(
    days: List<HomeContract.DayCell>,
    selectedDay: Int,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var hasScrolledToToday by remember { mutableStateOf(false) }

    LaunchedEffect(days) {
        if (hasScrolledToToday || days.isEmpty()) return@LaunchedEffect
        val todayIndex = days.indexOfFirst { it.isToday }
        if (todayIndex >= 0) {
            hasScrolledToToday = true
            listState.scrollToItem((todayIndex - LEADING_DAYS_BEFORE_TODAY).coerceAtLeast(0))
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
        Text(
            text = stringResource(Res.string.home_days_title),
            style = DSTheme.typography.textXl,
            color = DSTheme.colors.text,
        )
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.space3),
        ) {
            items(items = days, key = { it.dayNumber }) { day ->
                DayCellItem(
                    day = day,
                    isSelected = day.dayNumber == selectedDay,
                    onClick = { onDayClick(day.dayNumber) },
                )
            }
        }
    }
}

private const val LEADING_DAYS_BEFORE_TODAY = 2

@Composable
private fun DayCellItem(day: HomeContract.DayCell, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    val backgroundColor = if (isSelected) colors.accent else Color.Transparent
    val (weekdayColor, numberColor) = when {
        isSelected -> colors.accentOn to colors.accentOn
        day.isFilled -> colors.textSecondary to colors.text
        else -> colors.textTertiary to colors.textTertiary
    }
    val dotColor = when {
        isSelected -> Color.Transparent
        day.isFilled -> colors.accent
        else -> Color.Transparent
    }
    val todayBorderModifier = if (day.isToday && !isSelected) {
        Modifier.border(width = 1.dp, color = colors.accent, shape = DSShapes.lg)
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .width(46.dp)
            .clip(DSShapes.lg)
            .background(backgroundColor)
            .then(todayBorderModifier)
            .clickable(onClick = onClick)
            .padding(vertical = DSSpacing.space3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DSSpacing.space1),
    ) {
        Text(text = day.weekdayAbbreviation, style = DSTheme.typography.text2xs, color = weekdayColor)
        Text(text = "${day.dayNumber}", style = DSTheme.typography.textXl, color = numberColor)
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
    }
}

private fun previewDays(): List<HomeContract.DayCell> {
    val weekdays = listOf("SA", "SU", "MO", "TU", "WE", "TH", "FR")
    return (1..7).map { day ->
        HomeContract.DayCell(
            dayNumber = day,
            weekdayAbbreviation = weekdays[(day - 1) % weekdays.size],
            isFilled = day <= 4,
            isToday = day == 4,
        )
    }
}

@Preview
@Composable
private fun DayStripPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            DayStrip(days = previewDays(), selectedDay = 2, onDayClick = {}, modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview
@Composable
private fun DayStripPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            DayStrip(days = previewDays(), selectedDay = 2, onDayClick = {}, modifier = Modifier.padding(16.dp))
        }
    }
}
