package dev.whole30journal.feature.example.presentation.viewcontroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.whole30journal.feature.example.presentation.ui.ExampleScreen
import dev.whole30journal.feature.example.presentation.vm.ExampleViewModel
import platform.UIKit.UIViewController

/**
 * Wraps the shared Compose Multiplatform [ExampleScreen] in a UIViewController so it can be
 * embedded from SwiftUI via `UIViewControllerRepresentable` (see ComposeViewController.swift in iosApp).
 */
@Suppress("FunctionName", "unused")
fun ExampleScreenViewController(viewModel: ExampleViewModel): UIViewController =
    ComposeUIViewController {
        ExampleRoot(viewModel = viewModel)
    }

@Composable
private fun ExampleRoot(viewModel: ExampleViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ExampleScreen(
        state = state,
        onUiAction = { viewModel.onUiAction(it) },
        onUiEventConsume = { viewModel.onUiEventConsumed(it) },
        modifier = modifier,
    )
}
