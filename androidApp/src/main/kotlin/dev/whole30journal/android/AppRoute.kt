package dev.whole30journal.android

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object SettingsRoute

@Serializable
data class DayDetailRoute(val dayNumber: Int)

@Serializable
data class DayEntryRoute(val dayNumber: Int)
