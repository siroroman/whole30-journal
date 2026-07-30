package dev.whole30journal.feature.example.presentation.vm

import androidx.compose.runtime.Immutable
import dev.whole30journal.core.uistate.UiActionAware
import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.feature.example.domain.CatFact

object ExampleContract {

    @Immutable
    data class UiData(
        val catFact: CatFact? = null
    ) : UiStateAware.UiData

    sealed interface UiAction : UiActionAware.UiAction {
        data object OnAppear : UiAction
        data object OnRefreshClick : UiAction
    }

    sealed interface UiEvent : UiStateAware.UiEvent {
        data class ShowError(val message: String) : UiEvent
    }

    sealed interface OutputEvent : UiStateAware.OutputEvent
}
