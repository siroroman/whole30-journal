package dev.whole30journal.feature.home.presentation.vm

import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.core.uistate.vm.StateFlowViewModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class HomeViewModel :
    StateFlowViewModel<HomeContract.UiData, HomeContract.UiAction, HomeContract.UiEvent, HomeContract.OutputEvent>(
        initialState = UiStateAware.UiState(isLoading = false, uiData = buildHardcodedUiData())
    ) {

    override suspend fun applyUiAction(uiAction: HomeContract.UiAction) {
        when (uiAction) {
            is HomeContract.UiAction.OnDayClick ->
                updateUiData { copy(selectedDay = uiAction.dayNumber) }
            is HomeContract.UiAction.OnEditDayClick,
            is HomeContract.UiAction.OnViewDayDetailsClick,
            -> Unit // No Detail/Entry screen exists yet - documents intent for future navigation.
            is HomeContract.UiAction.OnTrendMetricSelected ->
                updateUiData { copy(selectedTrendMetric = uiAction.metric) }
        }
    }
}

private const val CURRENT_DAY = 12
private const val TOTAL_DAYS = 30
private const val PROGRESS_PERCENT = 40
private const val MISSING_ENTRY_DAY = 5

@OptIn(ExperimentalTime::class)
private val TODAY: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
private val PROGRAM_START_DATE: LocalDate = TODAY.plus(-(CURRENT_DAY - 1), DateTimeUnit.DAY)

private val OVERALL_VALUES = listOf(3, 4, 3, 5, 5, 6, 6, 7, 7, 8, 8, 8)
private val ENERGY_VALUES = listOf(3, 3, 2, 4, 5, 6, 6, 7, 7, 8, 8, 8)
private val MOOD_VALUES = listOf(4, 4, 3, 5, 6, 6, 7, 7, 8, 8, 8, 8)
private val SLEEP_VALUES = listOf(4, 5, 4, 6, 6, 7, 7, 8, 7, 8, 8, 8)
private val CRAVINGS_VALUES = listOf(2, 3, 3, 4, 5, 5, 6, 6, 7, 7, 8, 8)

private fun weekdayAbbreviation(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "MO"
    DayOfWeek.TUESDAY -> "TU"
    DayOfWeek.WEDNESDAY -> "WE"
    DayOfWeek.THURSDAY -> "TH"
    DayOfWeek.FRIDAY -> "FR"
    DayOfWeek.SATURDAY -> "SA"
    DayOfWeek.SUNDAY -> "SU"
}

private fun buildDayCells(): List<HomeContract.DayCell> = (1..TOTAL_DAYS).map { day ->
    val date = PROGRAM_START_DATE.plus(day - 1, DateTimeUnit.DAY)
    HomeContract.DayCell(
        dayNumber = day,
        date = date,
        weekdayAbbreviation = weekdayAbbreviation(date.dayOfWeek),
        isFilled = day <= CURRENT_DAY && day != MISSING_ENTRY_DAY,
        isToday = day == CURRENT_DAY,
    )
}

private fun series(values: List<Int>): List<HomeContract.TrendPoint> =
    values.mapIndexed { index, value -> HomeContract.TrendPoint(dayNumber = index + 1, value = value) }

private fun buildTrendSeries(): Map<HomeContract.TrendMetric, List<HomeContract.TrendPoint>> = mapOf(
    HomeContract.TrendMetric.Overall to series(OVERALL_VALUES),
    HomeContract.TrendMetric.Energy to series(ENERGY_VALUES),
    HomeContract.TrendMetric.Sleep to series(SLEEP_VALUES),
    HomeContract.TrendMetric.Cravings to series(CRAVINGS_VALUES),
)

private fun buildMetricsByDay(): Map<Int, HomeContract.DayMetrics> =
    (1..CURRENT_DAY).filter { it != MISSING_ENTRY_DAY }.associateWith { day ->
        val index = day - 1
        HomeContract.DayMetrics(
            overall = OVERALL_VALUES[index],
            energy = ENERGY_VALUES[index],
            mood = MOOD_VALUES[index],
            sleep = SLEEP_VALUES[index],
            cravings = CRAVINGS_VALUES[index],
        )
    }

private fun buildHardcodedUiData(): HomeContract.UiData = HomeContract.UiData(
    currentDay = CURRENT_DAY,
    totalDays = TOTAL_DAYS,
    progressPercent = PROGRESS_PERCENT,
    today = TODAY,
    days = buildDayCells(),
    selectedDay = 1,
    metricsByDay = buildMetricsByDay(),
    trendSeries = buildTrendSeries(),
)
