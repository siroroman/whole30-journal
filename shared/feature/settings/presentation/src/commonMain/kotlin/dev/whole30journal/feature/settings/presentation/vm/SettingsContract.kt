package dev.whole30journal.feature.settings.presentation.vm

import androidx.compose.runtime.Immutable
import dev.whole30journal.core.uistate.UiActionAware
import dev.whole30journal.core.uistate.UiStateAware
import kotlinx.datetime.LocalDate

object SettingsContract {

    enum class Mode { Setup, Edit }

    @Immutable
    data class UiData(
        val mode: Mode = Mode.Setup,
        val startDate: LocalDate? = null,
        val startDateLabel: String = "",
        val durationDays: Int = 30,
        val endDateLabel: String = "",
        val isDatePickerVisible: Boolean = false,
        val isSaving: Boolean = false,
    ) : UiStateAware.UiData

    sealed interface UiAction : UiActionAware.UiAction {
        data object OnChangeStartDateClick : UiAction
        data class OnStartDateSelected(val date: LocalDate) : UiAction
        data object OnDatePickerDismiss : UiAction
        data class OnDurationSelected(val durationDays: Int) : UiAction
        data object OnConfirmClick : UiAction
        data object OnCancelClick : UiAction
    }

    sealed interface UiEvent : UiStateAware.UiEvent {
        data class ShowSaveError(val message: String) : UiEvent
    }

    sealed interface OutputEvent : UiStateAware.OutputEvent {
        data object Saved : OutputEvent
        data object Cancelled : OutputEvent
    }
}
