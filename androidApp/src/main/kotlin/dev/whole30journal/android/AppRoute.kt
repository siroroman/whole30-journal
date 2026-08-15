package dev.whole30journal.android

import kotlinx.serialization.Serializable

sealed interface AppRoute {

    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data class DayDetail(val dayNumber: Int) : AppRoute

    @Serializable
    data class DayEntry(val dayNumber: Int) : AppRoute
}
