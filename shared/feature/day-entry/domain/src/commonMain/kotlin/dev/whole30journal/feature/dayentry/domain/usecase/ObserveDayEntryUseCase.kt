package dev.whole30journal.feature.dayentry.domain.usecase

import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import dev.whole30journal.feature.dayentry.domain.repository.DayEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class ObserveDayEntryUseCase(
    private val repository: DayEntryRepository
) {
    operator fun invoke(date: LocalDate): Flow<Result<DayEntry?>> = repository.observeDayEntry(date)
}
