package dev.whole30journal.feature.home.presentation.vm

import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.core.uistate.vm.StateFlowViewModel

class HomeViewModel :
    StateFlowViewModel<HomeContract.UiData, HomeContract.UiAction, HomeContract.UiEvent, HomeContract.OutputEvent>(
        initialState = UiStateAware.UiState(isLoading = false, uiData = buildHardcodedUiData())
    ) {

    override suspend fun applyUiAction(uiAction: HomeContract.UiAction) {
        when (uiAction) {
            is HomeContract.UiAction.OnDayClick,
            HomeContract.UiAction.OnEditTodayClick,
            HomeContract.UiAction.OnViewTodayDetailsClick,
            -> Unit // No Detail/Entry screen exists yet - documents intent for future navigation.
            is HomeContract.UiAction.OnTrendMetricSelected ->
                updateUiData { copy(selectedTrendMetric = uiAction.metric) }
        }
    }
}

private val WEEKDAYS_FROM_DAY_ONE = listOf("TH", "FR", "SA", "SU", "MO", "TU", "WE")
private const val CURRENT_DAY = 12
private const val TOTAL_DAYS = 30
private const val PROGRESS_PERCENT = 40

private fun buildDayCells(): List<HomeContract.DayCell> = (1..TOTAL_DAYS).map { day ->
    HomeContract.DayCell(
        dayNumber = day,
        weekdayAbbreviation = WEEKDAYS_FROM_DAY_ONE[(day - 1) % WEEKDAYS_FROM_DAY_ONE.size],
        isFilled = day <= CURRENT_DAY,
        isToday = day == CURRENT_DAY,
    )
}

private fun series(values: List<Int>): List<HomeContract.TrendPoint> =
    values.mapIndexed { index, value -> HomeContract.TrendPoint(dayNumber = index + 1, value = value) }

private fun buildTrendSeries(): Map<HomeContract.TrendMetric, List<HomeContract.TrendPoint>> = mapOf(
    HomeContract.TrendMetric.Overall to series(listOf(3, 4, 3, 5, 5, 6, 6, 7, 7, 8, 8, 8)),
    HomeContract.TrendMetric.Energy to series(listOf(3, 3, 2, 4, 5, 6, 6, 7, 7, 8, 8, 8)),
    HomeContract.TrendMetric.Sleep to series(listOf(4, 5, 4, 6, 6, 7, 7, 8, 7, 8, 8, 8)),
    HomeContract.TrendMetric.Cravings to series(listOf(2, 3, 3, 4, 5, 5, 6, 6, 7, 7, 8, 8)),
)

private fun buildHardcodedUiData(): HomeContract.UiData = HomeContract.UiData(
    currentDay = CURRENT_DAY,
    totalDays = TOTAL_DAYS,
    progressPercent = PROGRESS_PERCENT,
    days = buildDayCells(),
    today = HomeContract.TodayMetrics(overall = 8, energy = 8, mood = 8, sleep = 8, cravings = 8),
    trendSeries = buildTrendSeries(),
)
