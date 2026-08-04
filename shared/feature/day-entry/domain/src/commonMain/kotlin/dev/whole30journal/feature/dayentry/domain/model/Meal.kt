package dev.whole30journal.feature.dayentry.domain.model

data class Meal(
    val id: String,
    val label: String,
    val mealDescription: String,
    val photoToken: String?,
    val lovedIt: Boolean,
    val sortOrder: Long,
)
