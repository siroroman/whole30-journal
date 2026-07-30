package dev.whole30journal.feature.example.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.feature.example.presentation.generated.resources.Res
import dev.whole30journal.feature.example.presentation.generated.resources.example_button_get_cat_fact
import dev.whole30journal.feature.example.presentation.vm.ExampleContract
import org.jetbrains.compose.resources.stringResource

/**
 * Written once, rendered natively on both platforms: embedded directly in Android's Compose tree,
 * and wrapped in a UIViewController for iOS (see ExampleScreenViewController.kt in iosMain).
 */
@Composable
fun ExampleScreen(
    state: UiStateAware.UiState<ExampleContract.UiData, ExampleContract.UiEvent>,
    onUiAction: (ExampleContract.UiAction) -> Unit,
    onUiEventConsume: (ExampleContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        ExampleContent(
            uiData = state.uiData,
            isLoading = state.isLoading,
            onRefreshClick = { onUiAction(ExampleContract.UiAction.OnRefreshClick) },
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        )

        EventHandler(
            events = state.uiEvents,
            snackbarHostState = snackbarHostState,
            onConsume = onUiEventConsume
        )
    }
}

@Composable
private fun ExampleContent(
    uiData: ExampleContract.UiData,
    isLoading: Boolean,
    onRefreshClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(contentPadding).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            uiData.catFact?.let { catFact ->
                Text(
                    text = catFact.fact,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Button(
            onClick = onRefreshClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        ) {
            Text(stringResource(Res.string.example_button_get_cat_fact))
        }
    }
}

@Composable
private fun EventHandler(
    events: List<ExampleContract.UiEvent>,
    snackbarHostState: SnackbarHostState,
    onConsume: (ExampleContract.UiEvent) -> Unit,
) {
    events.forEach { event ->
        when (event) {
            is ExampleContract.UiEvent.ShowError -> {
                LaunchedEffect(event) {
                    snackbarHostState.showSnackbar(event.message)
                    onConsume(event)
                }
            }
        }
    }
}
