package dev.whole30journal.feature.home.presentation.vm

import androidx.compose.runtime.Immutable
import dev.whole30journal.core.uistate.UiActionAware
import dev.whole30journal.core.uistate.UiStateAware
import kotlinx.datetime.LocalDate

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
    data class TrendAxisLabels(
        val start: String = "",
        val middle: String = "",
        val end: String = "",
    )

    @Immutable
    data class UiData(
        val needsSetup: Boolean = false,
        val programStartDate: LocalDate? = null,
        val currentDay: Int = 0,
        val totalDays: Int = 30,
        val progressPercent: Int = 0,
        val days: List<DayCell> = emptyList(),
        val selectedDay: Int = 0,
        val selectedDayLabel: String = "",
        val progressStartLabel: String = "",
        val progressEndLabel: String = "",
        val trendAxisLabels: TrendAxisLabels = TrendAxisLabels(),
        val metricsByDay: Map<Int, DayMetrics> = emptyMap(),
        val selectedTrendMetric: TrendMetric = TrendMetric.Overall,
        val trendSeries: Map<TrendMetric, List<TrendPoint>> = emptyMap(),
    ) : UiStateAware.UiData

    sealed interface UiAction : UiActionAware.UiAction {
        data class OnDayClick(val dayNumber: Int) : UiAction
        data class OnEditDayClick(val dayNumber: Int) : UiAction
        data class OnViewDayDetailsClick(val dayNumber: Int) : UiAction
        data class OnTrendMetricSelected(val metric: TrendMetric) : UiAction
        data object OnSettingsClick : UiAction
    }

    /** No cases yet - nothing async, nothing that can fail with hardcoded data. */
    sealed interface UiEvent : UiStateAware.UiEvent

    sealed interface OutputEvent : UiStateAware.OutputEvent {
        data class NavigateToDayEntry(val dayNumber: Int) : OutputEvent
        data object NavigateToSettings : OutputEvent
    }
}
