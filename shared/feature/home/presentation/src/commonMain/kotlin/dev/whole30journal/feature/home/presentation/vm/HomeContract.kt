package dev.whole30journal.feature.home.presentation.vm

import androidx.compose.runtime.Immutable
import dev.whole30journal.core.uistate.UiActionAware
import dev.whole30journal.core.uistate.UiStateAware

object HomeContract {

    enum class TrendMetric { Overall, Energy, Sleep, Cravings }

    @Immutable
    data class DayCell(
        val dayNumber: Int,
        val weekdayAbbreviation: String,
        val isFilled: Boolean,
        val isToday: Boolean,
    )

    @Immutable
    data class DayMetrics(
        val overall: Int?,
        val energy: Int?,
        val mood: Int?,
        val sleep: Int?,
        val cravings: Int?,
    )

    @Immutable
    data class TrendPoint(val dayNumber: Int, val value: Int)

    @Immutable
    data class UiData(
        val currentDay: Int = 0,
        val totalDays: Int = 30,
        val progressPercent: Int = 0,
        val days: List<DayCell> = emptyList(),
        val selectedDay: Int = 0,
        val metricsByDay: Map<Int, DayMetrics> = emptyMap(),
        val selectedTrendMetric: TrendMetric = TrendMetric.Overall,
        val trendSeries: Map<TrendMetric, List<TrendPoint>> = emptyMap(),
    ) : UiStateAware.UiData

    sealed interface UiAction : UiActionAware.UiAction {
        data class OnDayClick(val dayNumber: Int) : UiAction
        data class OnEditDayClick(val dayNumber: Int) : UiAction
        data class OnViewDayDetailsClick(val dayNumber: Int) : UiAction
        data class OnTrendMetricSelected(val metric: TrendMetric) : UiAction
    }

    /** No cases yet - nothing async, nothing that can fail with hardcoded data. */
    sealed interface UiEvent : UiStateAware.UiEvent

    /** No cases yet - no Detail/Entry screen exists to navigate to. */
    sealed interface OutputEvent : UiStateAware.OutputEvent
}
