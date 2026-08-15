package dev.whole30journal.android.dayentry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.whole30journal.feature.dayentry.presentation.ui.DayEntryScreen
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryContract
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DayEntryRoute(
    dayNumber: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DayEntryViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(dayNumber) {
        viewModel.onUiAction(DayEntryContract.UiAction.OnAppear(dayNumber))
    }
    LaunchedEffect(viewModel) {
        viewModel.outputEvents.collect { event ->
            when (event) {
                DayEntryContract.OutputEvent.Close -> onClose()
            }
        }
    }

    DayEntryScreen(
        state = state,
        onUiAction = { viewModel.onUiAction(it) },
        onUiEventConsume = { viewModel.onUiEventConsumed(it) },
        modifier = modifier,
    )
}
