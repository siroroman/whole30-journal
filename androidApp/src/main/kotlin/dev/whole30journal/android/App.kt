package dev.whole30journal.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.whole30journal.feature.dayentry.presentation.ui.DayEntryScreen
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryContract
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryViewModel
import dev.whole30journal.feature.home.presentation.ui.HomeScreen
import dev.whole30journal.feature.home.presentation.vm.HomeContract
import dev.whole30journal.feature.home.presentation.vm.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val homeViewModel: HomeViewModel = koinViewModel()
    val homeState by homeViewModel.state.collectAsStateWithLifecycle()
    var editingDay by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(homeViewModel) {
        homeViewModel.outputEvents.collect { event ->
            when (event) {
                is HomeContract.OutputEvent.NavigateToDayEntry -> editingDay = event.dayNumber
            }
        }
    }

    val dayNumber = editingDay
    if (dayNumber != null) {
        val dayEntryViewModelStoreOwner = remember {
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        }
        DisposableEffect(Unit) {
            onDispose { dayEntryViewModelStoreOwner.viewModelStore.clear() }
        }

        CompositionLocalProvider(LocalViewModelStoreOwner provides dayEntryViewModelStoreOwner) {
            val dayEntryViewModel: DayEntryViewModel = koinViewModel()
            val dayEntryState by dayEntryViewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(dayNumber) {
                dayEntryViewModel.onUiAction(DayEntryContract.UiAction.OnAppear(dayNumber))
            }
            LaunchedEffect(dayEntryViewModel) {
                dayEntryViewModel.outputEvents.collect { event ->
                    when (event) {
                        DayEntryContract.OutputEvent.Close -> editingDay = null
                    }
                }
            }

            DayEntryScreen(
                state = dayEntryState,
                onUiAction = { dayEntryViewModel.onUiAction(it) },
                onUiEventConsume = { dayEntryViewModel.onUiEventConsumed(it) },
            )
        }
    } else {
        HomeScreen(
            state = homeState,
            onUiAction = { homeViewModel.onUiAction(it) },
            onUiEventConsume = { homeViewModel.onUiEventConsumed(it) },
        )
    }
}
