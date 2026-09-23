package dev.whole30journal.feature.dayentry.domain.repository

import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface DayEntryRepository {
    suspend fun getDayEntry(date: LocalDate): Result<DayEntry?>
    fun observeDayEntry(date: LocalDate): Flow<Result<DayEntry?>>
    suspend fun saveDayEntry(dayEntry: DayEntry): Result<Unit>
}
