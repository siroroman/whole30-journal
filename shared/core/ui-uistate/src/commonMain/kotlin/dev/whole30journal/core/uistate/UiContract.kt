package dev.whole30journal.core.uistate

/**
 * Namespaces the marker types shared by every MVI ViewModel contract in the app
 * (see ARCHITECTURE.md for an example contract).
 */
interface UiStateAware {

    interface UiData

    /** Transient state-carried event, consumed via [onUiEventConsumed]-style removal from [UiState.uiEvents]. */
    interface UiEvent

    /** One-shot side-effect (navigation, analytics, ...), delivered via a SharedFlow, never replayed. */
    interface OutputEvent

    data class UiState<S : UiData, E : UiEvent>(
        val isLoading: Boolean = true,
        val uiData: S,
        val uiEvents: List<E> = emptyList()
    )
}

interface UiActionAware<T : UiActionAware.UiAction> {

    fun onUiAction(uiAction: T)

    interface UiAction
}
