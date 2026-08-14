package dev.whole30journal.feature.dayentry.domain.model

data class DayEntry(
    val dayNumber: Long,
    val date: String,
    val metrics: List<Metric>,
    val notes: String,
    val isComplete: Boolean,
    val meals: List<Meal>,
    val achievements: List<Achievement>,
) {
    val isLogged: Boolean get() = metrics.isNotEmpty()
}
