package dev.whole30journal.android.home

import android.os.SystemClock
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.whole30journal.android.AppRoute
import dev.whole30journal.android.daydetail.DayDetailRoute
import dev.whole30journal.android.dayentry.DayEntryRoute
import dev.whole30journal.android.settings.SettingsRoute
import dev.whole30journal.feature.home.presentation.ui.HomeScreen
import dev.whole30journal.feature.home.presentation.vm.HomeContract
import dev.whole30journal.feature.home.presentation.vm.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

private const val NAV_ANIM_DURATION_MS = 300
private const val NAV_DEBOUNCE_MS = 1_000L

@Composable
fun HomeRoute() {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navigate = rememberDebouncedNavigate(navController)

    LaunchedEffect(viewModel, navigate) {
        viewModel.outputEvents.collect { event ->
            when (event) {
                is HomeContract.OutputEvent.NavigateToDayEntry -> navigate(AppRoute.DayEntry(event.dayNumber))
                is HomeContract.OutputEvent.NavigateToDayDetail -> navigate(AppRoute.DayDetail(event.dayNumber))
                HomeContract.OutputEvent.NavigateToSettings -> navigate(AppRoute.Settings)
            }
        }
    }

    when {
        state.isLoading -> Unit
        state.uiData.needsSetup -> SettingsRoute(onDone = {})
        else -> HomeNavHost(
            navController = navController,
            onNavigate = navigate,
            homeContent = {
                HomeScreen(
                    state = state,
                    onUiAction = { viewModel.onUiAction(it) },
                    onUiEventConsume = { viewModel.onUiEventConsumed(it) },
                )
            },
        )
    }
}

@Composable
private fun HomeNavHost(
    navController: NavHostController,
    onNavigate: (Any) -> Unit,
    homeContent: @Composable () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home,
        enterTransition = { slideIntoContainer(SlideDirection.Start, tween(NAV_ANIM_DURATION_MS)) },
        exitTransition = { slideOutOfContainer(SlideDirection.Start, tween(NAV_ANIM_DURATION_MS)) },
        popEnterTransition = { slideIntoContainer(SlideDirection.End, tween(NAV_ANIM_DURATION_MS)) },
        popExitTransition = { slideOutOfContainer(SlideDirection.End, tween(NAV_ANIM_DURATION_MS)) },
    ) {
        composable<AppRoute.Home> { homeContent() }

        composable<AppRoute.Settings> {
            SettingsRoute(onDone = { navController.popBackStack() })
        }

        composable<AppRoute.DayDetail> { backStackEntry ->
            DayDetailRoute(
                dayNumber = backStackEntry.toRoute<AppRoute.DayDetail>().dayNumber,
                onClose = { navController.popBackStack() },
                onEdit = { onNavigate(AppRoute.DayEntry(it)) },
            )
        }

        composable<AppRoute.DayEntry> { backStackEntry ->
            DayEntryRoute(
                dayNumber = backStackEntry.toRoute<AppRoute.DayEntry>().dayNumber,
                onClose = { navController.popBackStack() },
            )
        }
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
