@file:Suppress("TopLevelComposableFunctions")

package dev.whole30journal.core.uistate.vm

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import dev.whole30journal.core.uistate.UiActionAware
import dev.whole30journal.core.uistate.UiStateAware
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

actual abstract class BaseViewModel<
    S : UiStateAware.UiData,
    I : UiActionAware.UiAction,
    E : UiStateAware.UiEvent,
    O : UiStateAware.OutputEvent
    > actual constructor() : ViewModel(), BaseIntentViewModel<S, I, E, O> {

    @Composable
    protected actual abstract fun getState(): UiStateAware.UiState<S, E>

    actual override val state: StateFlow<UiStateAware.UiState<S, E>> by lazy(LazyThreadSafetyMode.NONE) {
        viewModelScope.launchMolecule(RecompositionMode.Immediate) { getState() }
    }

    /**
     * Cancels the children of the internal CoroutineScope. Call this from SwiftUI's `.onDisappear`
     * when the owning screen is popped, since iOS has no AndroidX-managed ViewModelStore to do it
     * automatically.
     */
    override fun clearScope() {
        viewModelScope.coroutineContext.cancelChildren()
        onCleared()
    }
}

actual interface BaseIntentViewModel<
    S : UiStateAware.UiData,
    I : UiActionAware.UiAction,
    E : UiStateAware.UiEvent,
    O : UiStateAware.OutputEvent
    > : UiActionAware<I> {

    actual val state: StateFlow<UiStateAware.UiState<S, E>>
    actual val outputEvents: SharedFlow<O>

    fun clearScope()
}
