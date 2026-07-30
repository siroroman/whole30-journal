@file:Suppress("TopLevelComposableFunctions")

package dev.whole30journal.core.uistate.vm

import androidx.compose.runtime.Composable
import dev.whole30journal.core.uistate.UiActionAware
import dev.whole30journal.core.uistate.UiStateAware
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel as AndroidXViewModel

/**
 * Kotlin/Native equivalent to the AndroidX `ViewModel`, so the exact same VM subclass can be
 * instantiated and driven from both Android (as a real AndroidX ViewModel) and iOS (as a plain
 * Kotlin object owned by Swift). The [state] StateFlow is produced by running [getState] - a
 * @Composable function - through Molecule, which lets state be *composed* the same way Compose UI
 * would render it, without needing to hand-roll a reducer.
 */
expect abstract class BaseViewModel<
    S : UiStateAware.UiData,
    I : UiActionAware.UiAction,
    E : UiStateAware.UiEvent,
    O : UiStateAware.OutputEvent
    >() : AndroidXViewModel, BaseIntentViewModel<S, I, E, O> {

    override val state: StateFlow<UiStateAware.UiState<S, E>>

    @Composable
    protected abstract fun getState(): UiStateAware.UiState<S, E>
}

expect interface BaseIntentViewModel<
    S : UiStateAware.UiData,
    I : UiActionAware.UiAction,
    E : UiStateAware.UiEvent,
    O : UiStateAware.OutputEvent
    > : UiActionAware<I> {

    val state: StateFlow<UiStateAware.UiState<S, E>>
    val outputEvents: SharedFlow<O>
}
