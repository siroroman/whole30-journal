package dev.whole30journal.feature.daydetail.presentation.viewcontroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.whole30journal.feature.daydetail.presentation.ui.DayDetailScreen
import dev.whole30journal.feature.daydetail.presentation.vm.DayDetailViewModel
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
fun DayDetailScreenViewController(viewModel: DayDetailViewModel): UIViewController =
    ComposeUIViewController {
        DayDetailRoot(viewModel = viewModel)
    }

@Composable
private fun DayDetailRoot(viewModel: DayDetailViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DayDetailScreen(
        state = state,
        onUiAction = { viewModel.onUiAction(it) },
        onUiEventConsume = { viewModel.onUiEventConsumed(it) },
        modifier = modifier,
    )
}
