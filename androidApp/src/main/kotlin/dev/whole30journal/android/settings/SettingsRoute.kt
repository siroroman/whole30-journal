package dev.whole30journal.android.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.whole30journal.feature.settings.presentation.ui.SettingsScreen
import dev.whole30journal.feature.settings.presentation.vm.SettingsContract
import dev.whole30journal.feature.settings.presentation.vm.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(onDone: () -> Unit, modifier: Modifier = Modifier) {
    CompositionLocalProvider(LocalViewModelStoreOwner provides rememberScopedViewModelStoreOwner()) {
        val viewModel: SettingsViewModel = koinViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(viewModel) {
            viewModel.outputEvents.collect { event ->
                when (event) {
                    SettingsContract.OutputEvent.Saved, SettingsContract.OutputEvent.Cancelled -> onDone()
                }
            }
        }

        SettingsScreen(
            state = state,
            onUiAction = { viewModel.onUiAction(it) },
            onUiEventConsume = { viewModel.onUiEventConsumed(it) },
            modifier = modifier,
        )
    }
}

@Composable
private fun rememberScopedViewModelStoreOwner(): ViewModelStoreOwner {
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(Unit) {
        onDispose { owner.viewModelStore.clear() }
    }
    return owner
}
