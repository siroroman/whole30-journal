package dev.whole30journal.feature.program.domain.repository

import dev.whole30journal.feature.program.domain.model.Program
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ProgramRepository {
    suspend fun getProgram(): Result<Program?>
    fun observeProgram(): Flow<Result<Program?>>

    /** Sets up the program for [startDate]/[durationDays] and seeds an empty day entry for each
     * of its days - see the implementation for the exact seeding semantics. */
    suspend fun configureProgram(startDate: LocalDate, durationDays: Long): Result<Program>
}
