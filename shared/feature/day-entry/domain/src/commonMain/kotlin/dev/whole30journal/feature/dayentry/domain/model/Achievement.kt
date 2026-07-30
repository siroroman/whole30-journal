package dev.whole30journal.feature.dayentry.domain.model

data class Achievement(
    val id: String,
    val dayNumber: Long,
    val text: String,
    val sortOrder: Long,
)
