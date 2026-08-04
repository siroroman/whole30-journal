package dev.whole30journal.feature.dayentry.domain.repository

import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import kotlinx.coroutines.flow.Flow

interface DayEntryRepository {
    suspend fun getDayEntry(dayNumber: Long): Result<DayEntry?>
    fun observeDayEntry(dayNumber: Long): Flow<Result<DayEntry?>>
    suspend fun saveDayEntry(dayEntry: DayEntry): Result<Unit>
}
