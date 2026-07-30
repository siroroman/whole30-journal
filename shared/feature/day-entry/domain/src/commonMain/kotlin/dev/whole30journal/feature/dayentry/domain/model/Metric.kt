package dev.whole30journal.feature.dayentry.domain.model

data class Metric(
    val title: String,
    val iconName: String,
    val value: Long?,
    val maxValue: Long,
    val note: String,
)
