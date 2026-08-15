package dev.whole30journal.core.uistate.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whole30journal.core.uistate.UiActionAware
import dev.whole30journal.core.uistate.UiStateAware
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class StateFlowViewModel<
    S : UiStateAware.UiData,
    I : UiActionAware.UiAction,
    E : UiStateAware.UiEvent,
    O : UiStateAware.OutputEvent
    >(
    initialState: UiStateAware.UiState<S, E>
) : ViewModel(), UiActionAware<I> {

    private val _uiState = MutableStateFlow(initialState)
    val state: StateFlow<UiStateAware.UiState<S, E>> = _uiState.asStateFlow()

    private val _outputEvents = MutableSharedFlow<O>()
    val outputEvents: SharedFlow<O> = _outputEvents.asSharedFlow()

    protected val currentUiState: UiStateAware.UiState<S, E>
        get() = _uiState.value

    protected val currentUiData: S
        get() = _uiState.value.uiData

    override fun onUiAction(uiAction: I) {
        viewModelScope.launch {
            applyUiAction(uiAction)
        }
    }

    protected abstract suspend fun applyUiAction(uiAction: I)

    fun onUiEventConsumed(uiEvent: E) {
        _uiState.update { uiState ->
            uiState.copy(uiEvents = uiState.uiEvents - uiEvent)
        }
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

    protected fun updateIsLoading(isLoading: Boolean) = updateUi(isLoading = isLoading)

    protected fun updateUiData(isLoading: Boolean? = null, uiData: S.() -> S) =
        updateUi(isLoading = isLoading, uiData = uiData)

    protected fun updateUiEvents(isLoading: Boolean? = null, uiEvents: (uiEvents: List<E>) -> List<E>) =
        updateUi(isLoading = isLoading, uiEvents = uiEvents)

    protected fun emitOutputEvent(event: O) {
        viewModelScope.launch {
            _outputEvents.emit(event)
        }
    }

    fun clearScope() {
        viewModelScope.coroutineContext.cancelChildren()
        onCleared()
    }
}
