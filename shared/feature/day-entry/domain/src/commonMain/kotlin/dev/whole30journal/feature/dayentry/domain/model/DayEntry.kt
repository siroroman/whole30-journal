package dev.whole30journal.feature.dayentry.domain.model

import kotlinx.datetime.LocalDate

data class DayEntry(
    val date: LocalDate,
    val metrics: List<Metric>,
    val notes: String,
    val isComplete: Boolean,
    val meals: List<Meal>,
    val achievements: List<Achievement>,
) {
    val isLogged: Boolean get() = metrics.isNotEmpty()
}
