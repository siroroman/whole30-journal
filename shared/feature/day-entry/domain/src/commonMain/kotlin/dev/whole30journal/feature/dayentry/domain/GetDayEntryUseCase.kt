package dev.whole30journal.feature.dayentry.domain

class GetDayEntryUseCase(
    private val repository: DayEntryRepository
) {
    suspend operator fun invoke(dayNumber: Long): Result<DayEntry?> = repository.getDayEntry(dayNumber)
}
