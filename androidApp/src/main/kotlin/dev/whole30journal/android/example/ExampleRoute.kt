package dev.whole30journal.android.example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.whole30journal.feature.example.presentation.ui.ExampleScreen
import dev.whole30journal.feature.example.presentation.vm.ExampleContract
import dev.whole30journal.feature.example.presentation.vm.ExampleViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Embeds the *exact same* [ExampleScreen] composable that iOS wraps in a UIViewController -
 * Pattern A ("shared Compose Multiplatform UI") from ARCHITECTURE.md.
 */
@Composable
fun ExampleRoute(modifier: Modifier = Modifier) {
    val viewModel: ExampleViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onUiAction(ExampleContract.UiAction.OnAppear)
    }

    ExampleScreen(
        state = state,
        onUiAction = { viewModel.onUiAction(it) },
        onUiEventConsume = { viewModel.onUiEventConsumed(it) },
        modifier = modifier,
    )
}
