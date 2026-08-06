package dev.whole30journal.feature.home.presentation.vm

import androidx.lifecycle.viewModelScope
import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.core.uistate.vm.StateFlowViewModel
import dev.whole30journal.core.utils.DateFormatter
import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import dev.whole30journal.feature.dayentry.domain.model.MetricTitle
import dev.whole30journal.feature.dayentry.domain.usecase.GetDayEntryUseCase
import dev.whole30journal.feature.program.domain.usecase.GetProgramUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class HomeViewModel(
    private val dateFormatter: DateFormatter,
    private val getProgram: GetProgramUseCase,
    private val getDayEntry: GetDayEntryUseCase,
    @OptIn(ExperimentalTime::class)
    private val clock: Clock = Clock.System,
) : StateFlowViewModel<HomeContract.UiData, HomeContract.UiAction, HomeContract.UiEvent, HomeContract.OutputEvent>(
    initialState = UiStateAware.UiState(isLoading = true, uiData = HomeContract.UiData())
) {

    init {
        viewModelScope.launch { loadHomeData() }
    }

    override suspend fun applyUiAction(uiAction: HomeContract.UiAction) {
        when (uiAction) {
            is HomeContract.UiAction.OnDayClick -> selectDay(uiAction.dayNumber)
            is HomeContract.UiAction.OnEditDayClick ->
                emitOutputEvent(HomeContract.OutputEvent.NavigateToDayEntry(uiAction.dayNumber))
            is HomeContract.UiAction.OnViewDayDetailsClick -> Unit
            is HomeContract.UiAction.OnTrendMetricSelected ->
                updateUiData { copy(selectedTrendMetric = uiAction.metric) }
        }
    }

    private suspend fun loadHomeData() {
        val program = getProgram().getOrNull()
        if (program == null) {
            updateIsLoading(false)
            return
        }

        val startDate = program.startDate
        val totalDays = program.durationDays.toInt()
        val currentDay = program.currentDayNumber.toInt()

        val entries = coroutineScope {
            (1..currentDay)
                .map { day -> day to async { getDayEntry(day.toLong()).getOrNull() } }
                .associate { (day, deferred) -> day to deferred.await() }
        }
        val metricsByDay = entries.mapNotNull { (day, entry) -> entry?.toDayMetrics()?.let { day to it } }.toMap()

        val days = (1..totalDays).map { day ->
            HomeContract.DayCell(
                dayNumber = day,
                weekdayAbbreviation = dateFormatter.weekdayAbbreviation(dateForDay(day, startDate).dayOfWeek),
                isFilled = metricsByDay.containsKey(day),
                isToday = day == currentDay,
            )
        }

        updateUiData(isLoading = false) {
            copy(
                programStartDate = startDate,
                currentDay = currentDay,
                totalDays = totalDays,
                progressPercent = currentDay * PROGRESS_PERCENT_SCALE / totalDays,
                days = days,
                selectedDay = currentDay,
                metricsByDay = metricsByDay,
                trendSeries = buildTrendSeries(metricsByDay),
            )
        }
        refreshDateLabels(currentDay, startDate, totalDays)
    }

    private suspend fun selectDay(dayNumber: Int) {
        val startDate = currentUiData.programStartDate ?: return
        val label = dateFormatter(dateForDay(dayNumber, startDate), today(), DateFormatter.Style.Long)
        updateUiData { copy(selectedDay = dayNumber, selectedDayLabel = label) }
    }

    private suspend fun refreshDateLabels(selectedDay: Int, startDate: LocalDate, totalDays: Int) {
        val today = today()
        val selectedDayLabel = dateFormatter(dateForDay(selectedDay, startDate), today, DateFormatter.Style.Long)
        val progressStartLabel = dateFormatter(dateForDay(1, startDate), today, DateFormatter.Style.Short)
        val progressEndLabel = dateFormatter(dateForDay(totalDays, startDate), today, DateFormatter.Style.Short)
        val trendAxisLabels = HomeContract.TrendAxisLabels(
            start = progressStartLabel,
            middle = dateFormatter(dateForDay(totalDays / 2, startDate), today, DateFormatter.Style.Short),
            end = progressEndLabel,
        )
        updateUiData {
            copy(
                selectedDayLabel = selectedDayLabel,
                progressStartLabel = progressStartLabel,
                progressEndLabel = progressEndLabel,
                trendAxisLabels = trendAxisLabels,
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun today(): LocalDate = clock.todayIn(TimeZone.currentSystemDefault())
}

private fun dateForDay(dayNumber: Int, startDate: LocalDate): LocalDate = startDate.plus(dayNumber - 1, DateTimeUnit.DAY)

private const val PROGRESS_PERCENT_SCALE = 100
private const val NORMALIZED_SCORE_SCALE = 10.0

private fun DayEntry.toDayMetrics(): HomeContract.DayMetrics? {
    if (metrics.isEmpty()) return null
    fun scoreFor(title: String): Int? = metrics.firstOrNull { it.title == title }?.let { metric ->
        val value = metric.value ?: return@let null
        if (metric.maxValue <= 0) return@let null
        (value.toDouble() / metric.maxValue * NORMALIZED_SCORE_SCALE).roundToInt()
    }
    return HomeContract.DayMetrics(
        overall = scoreFor(MetricTitle.OVERALL),
        energy = scoreFor(MetricTitle.ENERGY),
        mood = scoreFor(MetricTitle.MOOD),
        sleep = scoreFor(MetricTitle.SLEEP),
        cravings = scoreFor(MetricTitle.CRAVINGS),
    )
}

private fun buildTrendSeries(
    metricsByDay: Map<Int, HomeContract.DayMetrics>,
): Map<HomeContract.TrendMetric, List<HomeContract.TrendPoint>> {
    fun series(selector: (HomeContract.DayMetrics) -> Int?): List<HomeContract.TrendPoint> =
        metricsByDay.entries.sortedBy { it.key }.mapNotNull { (day, metrics) ->
            selector(metrics)?.let { HomeContract.TrendPoint(dayNumber = day, value = it) }
        }

    return mapOf(
        HomeContract.TrendMetric.Overall to series { it.overall },
        HomeContract.TrendMetric.Energy to series { it.energy },
        HomeContract.TrendMetric.Sleep to series { it.sleep },
        HomeContract.TrendMetric.Cravings to series { it.cravings },
    )
}
