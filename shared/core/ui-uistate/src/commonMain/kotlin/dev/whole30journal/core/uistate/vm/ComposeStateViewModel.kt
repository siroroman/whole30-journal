package dev.whole30journal.core.uistate.vm

import androidx.lifecycle.viewModelScope
import dev.whole30journal.core.uistate.UiActionAware
import dev.whole30journal.core.uistate.UiStateAware
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Wires the MVI plumbing (action dispatch + one-shot output events) on top of [BaseViewModel],
 * leaving [getState] free for subclasses to compose state however they like.
 *
 * Most screens don't need that flexibility - see [StateFlowViewModel] for the common case.
 */
abstract class ComposeStateViewModel<
    S : UiStateAware.UiData,
    I : UiActionAware.UiAction,
    E : UiStateAware.UiEvent,
    O : UiStateAware.OutputEvent
    > : BaseViewModel<S, I, E, O>() {

    private val _outputEvents = MutableSharedFlow<O>()
    override val outputEvents = _outputEvents.asSharedFlow()

    protected val currentUiState: UiStateAware.UiState<S, E>
        get() = state.value

    protected val currentUiData: S
        get() = state.value.uiData

    override fun onUiAction(uiAction: I) {
        viewModelScope.launch {
            applyUiAction(uiAction)
        }
    }

    protected abstract suspend fun applyUiAction(uiAction: I)

    abstract fun onUiEventConsumed(uiEvent: E)

    protected fun emitOutputEvent(event: O) {
        viewModelScope.launch {
            _outputEvents.emit(event)
        }
    }
}
