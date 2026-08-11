package dev.whole30journal.feature.settings.presentation.viewcontroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.whole30journal.feature.settings.presentation.ui.SettingsScreen
import dev.whole30journal.feature.settings.presentation.vm.SettingsViewModel
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
fun SettingsScreenViewController(viewModel: SettingsViewModel): UIViewController =
    ComposeUIViewController {
        SettingsRoot(viewModel = viewModel)
    }

@Composable
private fun SettingsRoot(viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onUiAction = { viewModel.onUiAction(it) },
        onUiEventConsume = { viewModel.onUiEventConsumed(it) },
        modifier = modifier,
    )
}
