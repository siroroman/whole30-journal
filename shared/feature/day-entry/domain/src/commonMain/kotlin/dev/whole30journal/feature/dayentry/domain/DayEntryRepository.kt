package dev.whole30journal.feature.dayentry.domain

import kotlinx.coroutines.flow.Flow

interface DayEntryRepository {
    suspend fun getDayEntry(dayNumber: Long): Result<DayEntry?>
    fun observeDayEntry(dayNumber: Long): Flow<DayEntry?>
    suspend fun saveDayEntry(dayEntry: DayEntry): Result<Unit>
}
