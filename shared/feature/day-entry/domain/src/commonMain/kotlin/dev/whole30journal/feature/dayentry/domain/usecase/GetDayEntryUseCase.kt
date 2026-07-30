package dev.whole30journal.feature.dayentry.domain.usecase

import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import dev.whole30journal.feature.dayentry.domain.repository.DayEntryRepository

class GetDayEntryUseCase(
    private val repository: DayEntryRepository
) {
    suspend operator fun invoke(dayNumber: Long): Result<DayEntry?> = repository.getDayEntry(dayNumber)
}
