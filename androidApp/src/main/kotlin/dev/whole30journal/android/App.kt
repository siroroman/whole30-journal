package dev.whole30journal.android

import android.os.SystemClock
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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

private const val NAV_ANIM_DURATION_MS = 300
private const val NAV_DEBOUNCE_MS = 1_000L

@Composable
fun App() {
    val homeViewModel: HomeViewModel = koinViewModel()
    val homeState by homeViewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navigate = rememberDebouncedNavigate(navController)

    LaunchedEffect(homeViewModel, navigate) {
        homeViewModel.outputEvents.collect { event ->
            when (event) {
                is HomeContract.OutputEvent.NavigateToDayEntry -> navigate(DayEntryRoute(event.dayNumber))
                is HomeContract.OutputEvent.NavigateToDayDetail -> navigate(DayDetailRoute(event.dayNumber))
                HomeContract.OutputEvent.NavigateToSettings -> navigate(SettingsRoute)
            }
        }
    }

    when {
        homeState.isLoading -> Unit
        homeState.uiData.needsSetup -> SettingsOverlay(onDone = {})
        else -> AppNavHost(
            navController = navController,
            onNavigate = navigate,
            homeContent = {
                HomeScreen(
                    state = homeState,
                    onUiAction = { homeViewModel.onUiAction(it) },
                    onUiEventConsume = { homeViewModel.onUiEventConsumed(it) },
                )
            },
        )
    }
}

@Composable
private fun rememberDebouncedNavigate(navController: NavHostController): (Any) -> Unit {
    val lastNavigateAtMs = remember { LongArray(1) }
    return remember(navController) {
        val debouncedNavigate: (Any) -> Unit = { route ->
            val now = SystemClock.elapsedRealtime()
            if (now - lastNavigateAtMs[0] >= NAV_DEBOUNCE_MS) {
                lastNavigateAtMs[0] = now
                navController.navigate(route)
            }
        }
        debouncedNavigate
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    onNavigate: (Any) -> Unit,
    homeContent: @Composable () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        enterTransition = { slideIntoContainer(SlideDirection.Start, tween(NAV_ANIM_DURATION_MS)) },
        exitTransition = { slideOutOfContainer(SlideDirection.Start, tween(NAV_ANIM_DURATION_MS)) },
        popEnterTransition = { slideIntoContainer(SlideDirection.End, tween(NAV_ANIM_DURATION_MS)) },
        popExitTransition = { slideOutOfContainer(SlideDirection.End, tween(NAV_ANIM_DURATION_MS)) },
    ) {
        composable<HomeRoute> { homeContent() }

        composable<SettingsRoute> {
            SettingsOverlay(onDone = { navController.popBackStack() })
        }

        composable<DayDetailRoute> { backStackEntry ->
            val dayNumber = backStackEntry.toRoute<DayDetailRoute>().dayNumber
            val dayDetailViewModel: DayDetailViewModel = koinViewModel()
            val dayDetailState by dayDetailViewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(dayNumber) {
                dayDetailViewModel.onUiAction(DayDetailContract.UiAction.OnAppear(dayNumber))
            }
            LaunchedEffect(dayDetailViewModel, onNavigate) {
                dayDetailViewModel.outputEvents.collect { event ->
                    when (event) {
                        DayDetailContract.OutputEvent.Close -> navController.popBackStack()
                        is DayDetailContract.OutputEvent.EditRequested -> onNavigate(DayEntryRoute(event.dayNumber))
                    }
                }
            }

            DayDetailScreen(
                state = dayDetailState,
                onUiAction = { dayDetailViewModel.onUiAction(it) },
                onUiEventConsume = { dayDetailViewModel.onUiEventConsumed(it) },
            )
        }

        composable<DayEntryRoute> { backStackEntry ->
            val dayNumber = backStackEntry.toRoute<DayEntryRoute>().dayNumber
            val dayEntryViewModel: DayEntryViewModel = koinViewModel()
            val dayEntryState by dayEntryViewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(dayNumber) {
                dayEntryViewModel.onUiAction(DayEntryContract.UiAction.OnAppear(dayNumber))
            }
            LaunchedEffect(dayEntryViewModel) {
                dayEntryViewModel.outputEvents.collect { event ->
                    when (event) {
                        DayEntryContract.OutputEvent.Close -> navController.popBackStack()
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
