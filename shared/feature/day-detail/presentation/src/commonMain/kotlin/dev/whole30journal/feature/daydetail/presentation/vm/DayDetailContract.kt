package dev.whole30journal.feature.daydetail.presentation.vm

import androidx.compose.runtime.Immutable
import dev.whole30journal.core.uistate.UiActionAware
import dev.whole30journal.core.uistate.UiStateAware

object DayDetailContract {

    @Immutable
    data class MetricSummary(val title: String, val note: String, val score: Int?)

    @Immutable
    data class MealSummary(
        val id: String,
        val label: String,
        val description: String,
        val photoToken: String?,
        val lovedIt: Boolean,
    )

    @Immutable
    data class UiData(
        val dayNumber: Int = 0,
        val dateLabel: String = "",
        val hasEntry: Boolean = false,
        val isComplete: Boolean = false,
        val overallScore: Int? = null,
        val metrics: List<MetricSummary> = emptyList(),
        val meals: List<MealSummary> = emptyList(),
        val achievements: List<String> = emptyList(),
        val notes: String = "",
    ) : UiStateAware.UiData

    sealed interface UiAction : UiActionAware.UiAction {
        data class OnAppear(val dayNumber: Int) : UiAction
        data object OnBackClick : UiAction
        data object OnEditClick : UiAction
    }

    /** No cases yet - nothing async, nothing that can fail while reading an entry. */
    sealed interface UiEvent : UiStateAware.UiEvent

    sealed interface OutputEvent : UiStateAware.OutputEvent {
        data object Close : OutputEvent
        data class EditRequested(val dayNumber: Int) : OutputEvent
    }
}
