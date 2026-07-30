package dev.whole30journal.feature.dayentry.domain

import kotlinx.coroutines.flow.Flow

class ObserveDayEntryUseCase(
    private val repository: DayEntryRepository
) {
    operator fun invoke(dayNumber: Long): Flow<DayEntry?> = repository.observeDayEntry(dayNumber)
}
