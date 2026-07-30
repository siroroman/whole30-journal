package dev.whole30journal.feature.dayentry.domain.model

data class Meal(
    val id: String,
    val dayNumber: Long,
    val label: String,
    val description: String,
    val photoToken: String?,
    val lovedIt: Boolean,
    val sortOrder: Long,
)
