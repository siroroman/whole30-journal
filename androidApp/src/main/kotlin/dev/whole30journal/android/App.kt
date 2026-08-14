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
import dev.whole30journal.feature.daydetail.presentation.ui.DayDetailScreen
import dev.whole30journal.feature.daydetail.presentation.vm.DayDetailContract
import dev.whole30journal.feature.daydetail.presentation.vm.DayDetailViewModel
import dev.whole30journal.feature.dayentry.presentation.ui.DayEntryScreen
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryContract
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryViewModel
import dev.whole30journal.feature.home.presentation.ui.HomeScreen
import dev.whole30journal.feature.home.presentation.vm.HomeContract
import dev.whole30journal.feature.home.presentation.vm.HomeViewModel
import dev.whole30journal.feature.settings.presentation.ui.SettingsScreen
import dev.whole30journal.feature.settings.presentation.vm.SettingsContract
import dev.whole30journal.feature.settings.presentation.vm.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val homeViewModel: HomeViewModel = koinViewModel()
    val homeState by homeViewModel.state.collectAsStateWithLifecycle()
    var editingDay by remember { mutableStateOf<Int?>(null) }
    var detailDay by remember { mutableStateOf<Int?>(null) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    LaunchedEffect(homeViewModel) {
        homeViewModel.outputEvents.collect { event ->
            when (event) {
                is HomeContract.OutputEvent.NavigateToDayEntry -> editingDay = event.dayNumber
                is HomeContract.OutputEvent.NavigateToDayDetail -> detailDay = event.dayNumber
                HomeContract.OutputEvent.NavigateToSettings -> isSettingsOpen = true
            }
        }
    }

    val dayNumber = editingDay
    val dayNumberForDetail = detailDay
    when {
        homeState.isLoading -> Unit
        homeState.uiData.needsSetup -> SettingsOverlay(onDone = {})
        isSettingsOpen -> SettingsOverlay(onDone = { isSettingsOpen = false })
        dayNumber != null -> {
            CompositionLocalProvider(LocalViewModelStoreOwner provides rememberScopedViewModelStoreOwner()) {
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
        }
        dayNumberForDetail != null -> {
            CompositionLocalProvider(LocalViewModelStoreOwner provides rememberScopedViewModelStoreOwner()) {
                val dayDetailViewModel: DayDetailViewModel = koinViewModel()
                val dayDetailState by dayDetailViewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(dayNumberForDetail) {
                    dayDetailViewModel.onUiAction(DayDetailContract.UiAction.OnAppear(dayNumberForDetail))
                }
                LaunchedEffect(dayDetailViewModel) {
                    dayDetailViewModel.outputEvents.collect { event ->
                        when (event) {
                            DayDetailContract.OutputEvent.Close -> detailDay = null
                            is DayDetailContract.OutputEvent.EditRequested -> editingDay = event.dayNumber
                        }
                    }
                }

                DayDetailScreen(
                    state = dayDetailState,
                    onUiAction = { dayDetailViewModel.onUiAction(it) },
                    onUiEventConsume = { dayDetailViewModel.onUiEventConsumed(it) },
                )
            }
        }
        else -> HomeScreen(
            state = homeState,
            onUiAction = { homeViewModel.onUiAction(it) },
            onUiEventConsume = { homeViewModel.onUiEventConsumed(it) },
        )
    }
}

@Composable
private fun SettingsOverlay(onDone: () -> Unit) {
    CompositionLocalProvider(LocalViewModelStoreOwner provides rememberScopedViewModelStoreOwner()) {
        val settingsViewModel: SettingsViewModel = koinViewModel()
        val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(settingsViewModel) {
            settingsViewModel.outputEvents.collect { event ->
                when (event) {
                    SettingsContract.OutputEvent.Saved, SettingsContract.OutputEvent.Cancelled -> onDone()
                }
            }
        }

        SettingsScreen(
            state = settingsState,
            onUiAction = { settingsViewModel.onUiAction(it) },
            onUiEventConsume = { settingsViewModel.onUiEventConsumed(it) },
        )
    }
}

@Composable
private fun rememberScopedViewModelStoreOwner(): ViewModelStoreOwner {
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(Unit) {
        onDispose { owner.viewModelStore.clear() }
    }
    return owner
}
