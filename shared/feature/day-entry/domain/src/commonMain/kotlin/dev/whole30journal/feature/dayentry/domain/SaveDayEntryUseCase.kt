package dev.whole30journal.feature.dayentry.domain

class SaveDayEntryUseCase(
    private val repository: DayEntryRepository
) {
    suspend operator fun invoke(dayEntry: DayEntry): Result<Unit> = repository.saveDayEntry(dayEntry)
}
