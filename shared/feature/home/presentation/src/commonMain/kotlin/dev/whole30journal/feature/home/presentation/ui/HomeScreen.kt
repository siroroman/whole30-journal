package dev.whole30journal.feature.home.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.feature.home.presentation.vm.HomeContract

@Composable
fun HomeScreen(
    state: UiStateAware.UiState<HomeContract.UiData, HomeContract.UiEvent>,
    onUiAction: (HomeContract.UiAction) -> Unit,
    onUiEventConsume: (HomeContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    DSTheme {
        Scaffold(modifier = modifier, containerColor = DSTheme.colors.bg) { contentPadding ->
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
            .padding(horizontal = DSSpacing.space7, vertical = DSSpacing.space7),
        verticalArrangement = Arrangement.spacedBy(DSSpacing.space8),
    ) {
        HomeProgressHeader(
            currentDay = uiData.currentDay,
            totalDays = uiData.totalDays,
            progressPercent = uiData.progressPercent,
            progressStartLabel = uiData.progressStartLabel,
            progressEndLabel = uiData.progressEndLabel,
            onSettingsClick = { onUiAction(HomeContract.UiAction.OnSettingsClick) },
        )
        DayStrip(
            days = uiData.days,
            selectedDay = uiData.selectedDay,
            onDayClick = { onUiAction(HomeContract.UiAction.OnDayClick(it)) },
        )
        DayOverviewCard(
            selectedDay = uiData.selectedDay,
            selectedDayLabel = uiData.selectedDayLabel,
            currentDay = uiData.currentDay,
            totalDays = uiData.totalDays,
            metrics = uiData.metricsByDay[uiData.selectedDay],
            onEditClick = { onUiAction(HomeContract.UiAction.OnEditDayClick(uiData.selectedDay)) },
            onViewDetailsClick = { onUiAction(HomeContract.UiAction.OnViewDayDetailsClick(uiData.selectedDay)) },
        )
        TrendsSection(
            selectedMetric = uiData.selectedTrendMetric,
            series = uiData.trendSeries[uiData.selectedTrendMetric].orEmpty(),
            totalDays = uiData.totalDays,
            trendAxisLabels = uiData.trendAxisLabels,
            onMetricSelect = { onUiAction(HomeContract.UiAction.OnTrendMetricSelected(it)) },
        )
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
        selectedDay = 1,
        selectedDayLabel = "25.7.2026",
        progressStartLabel = "25.7.2026",
        progressEndLabel = "23.8.2026",
        trendAxisLabels = HomeContract.TrendAxisLabels(start = "25.7.2026", middle = "8.8.2026", end = "23.8.2026"),
        metricsByDay = (1..currentDay).associateWith { day ->
            HomeContract.DayMetrics(
                overall = trendValues[(day - 1) % trendValues.size],
                energy = 7,
                mood = 8,
                sleep = 6,
                cravings = 3,
            )
        },
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
