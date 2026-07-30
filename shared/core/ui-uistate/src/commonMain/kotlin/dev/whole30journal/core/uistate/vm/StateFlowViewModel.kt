package dev.whole30journal.core.uistate.vm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.whole30journal.core.uistate.UiActionAware
import dev.whole30journal.core.uistate.UiStateAware
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base ViewModel for straightforward reactive state management: holds a [MutableStateFlow] seeded
 * with [initialState] and exposes small helpers ([updateUiData], [updateIsLoading], ...) to mutate
 * it. This is what most feature ViewModels extend (see ARCHITECTURE.md).
 */
abstract class StateFlowViewModel<
    S : UiStateAware.UiData,
    I : UiActionAware.UiAction,
    E : UiStateAware.UiEvent,
    O : UiStateAware.OutputEvent
    >(
    private val initialState: UiStateAware.UiState<S, E>
) : ComposeStateViewModel<S, I, E, O>() {

    private val _uiState = MutableStateFlow(initialState)

    @Composable
    override fun getState(): UiStateAware.UiState<S, E> {
        val state by _uiState.collectAsState()
        return state
    }

    protected fun updateUi(
        isLoading: Boolean? = null,
        uiData: ((uiData: S) -> S)? = null,
        uiEvents: ((uiEvents: List<E>) -> List<E>)? = null
    ) {
        _uiState.update { uiState ->
            uiState.copy(
                isLoading = isLoading ?: uiState.isLoading,
                uiData = uiData?.invoke(uiState.uiData) ?: uiState.uiData,
                uiEvents = uiEvents?.invoke(uiState.uiEvents) ?: uiState.uiEvents
            )
        }
    }

    protected fun updateIsLoading(isLoading: Boolean) {
        updateUi(isLoading = isLoading)
    }

    protected fun updateUiData(isLoading: Boolean? = null, uiData: S.() -> S) {
        updateUi(isLoading = isLoading, uiData = uiData)
    }

    protected fun updateUiEvents(isLoading: Boolean? = null, uiEvents: (uiEvents: List<E>) -> List<E>) {
        updateUi(isLoading = isLoading, uiEvents = uiEvents)
    }

    override fun onUiEventConsumed(uiEvent: E) {
        _uiState.update { uiState ->
            uiState.copy(uiEvents = uiState.uiEvents - uiEvent)
        }
    }
}
