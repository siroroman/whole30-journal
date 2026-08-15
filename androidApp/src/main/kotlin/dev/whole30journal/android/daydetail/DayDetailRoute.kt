package dev.whole30journal.android.daydetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.whole30journal.feature.daydetail.presentation.ui.DayDetailScreen
import dev.whole30journal.feature.daydetail.presentation.vm.DayDetailContract
import dev.whole30journal.feature.daydetail.presentation.vm.DayDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DayDetailRoute(
    dayNumber: Int,
    onClose: () -> Unit,
    onEdit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DayDetailViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(dayNumber) {
        viewModel.onUiAction(DayDetailContract.UiAction.OnAppear(dayNumber))
    }
    LaunchedEffect(viewModel) {
        viewModel.outputEvents.collect { event ->
            when (event) {
                DayDetailContract.OutputEvent.Close -> onClose()
                is DayDetailContract.OutputEvent.EditRequested -> onEdit(event.dayNumber)
            }
        }
    }

    DayDetailScreen(
        state = state,
        onUiAction = { viewModel.onUiAction(it) },
        onUiEventConsume = { viewModel.onUiEventConsumed(it) },
        modifier = modifier,
    )
}
