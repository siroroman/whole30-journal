package dev.whole30journal.feature.program.domain.model

import kotlinx.datetime.LocalDate

data class Program(
    val startDate: LocalDate,
    val durationDays: Long,
    val endDate: LocalDate,
    val currentDayNumber: Long,
)
