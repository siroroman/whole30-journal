package dev.whole30journal.feature.dayentry.presentation.viewcontroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.whole30journal.feature.dayentry.presentation.ui.DayEntryScreen
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryViewModel
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
fun DayEntryScreenViewController(viewModel: DayEntryViewModel): UIViewController =
    ComposeUIViewController {
        DayEntryRoot(viewModel = viewModel)
    }

@Composable
private fun DayEntryRoot(viewModel: DayEntryViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DayEntryScreen(
        state = state,
        onUiAction = { viewModel.onUiAction(it) },
        onUiEventConsume = { viewModel.onUiEventConsumed(it) },
        modifier = modifier,
    )
}
