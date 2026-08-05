package dev.whole30journal.feature.home.presentation.viewcontroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.whole30journal.feature.home.presentation.ui.HomeScreen
import dev.whole30journal.feature.home.presentation.vm.HomeViewModel
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
fun HomeScreenViewController(viewModel: HomeViewModel): UIViewController =
    ComposeUIViewController {
        HomeRoot(viewModel = viewModel)
    }

@Composable
private fun HomeRoot(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onUiAction = { viewModel.onUiAction(it) },
        onUiEventConsume = { viewModel.onUiEventConsumed(it) },
        modifier = modifier,
    )
}
