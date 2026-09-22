package dev.whole30journal.feature.dayentry.domain.usecase

import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import dev.whole30journal.feature.dayentry.domain.repository.DayEntryRepository
import kotlinx.datetime.LocalDate

class GetDayEntryUseCase(
    private val repository: DayEntryRepository
) {
    suspend operator fun invoke(date: LocalDate): Result<DayEntry?> = repository.getDayEntry(date)
}
