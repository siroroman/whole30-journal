package dev.whole30journal.feature.dayentry.domain.usecase

import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import dev.whole30journal.feature.dayentry.domain.repository.DayEntryRepository

class SaveDayEntryUseCase(
    private val repository: DayEntryRepository
) {
    suspend operator fun invoke(dayEntry: DayEntry): Result<Unit> = repository.saveDayEntry(dayEntry)
}
