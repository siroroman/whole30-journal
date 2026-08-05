package dev.whole30journal.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.whole30journal.feature.home.presentation.ui.HomeScreen
import dev.whole30journal.feature.home.presentation.vm.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root of the app - renders the shared Compose Multiplatform Home screen (see ARCHITECTURE.md,
 * Pattern A). HomeScreen applies Whole30Theme itself, since the same composable is also embedded
 * from iOS with no SwiftUI-side theming equivalent.
 */
@Composable
fun App() {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onUiAction = { viewModel.onUiAction(it) },
        onUiEventConsume = { viewModel.onUiEventConsumed(it) },
    )
}
