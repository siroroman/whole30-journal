package dev.whole30journal.feature.program.domain.repository

import dev.whole30journal.feature.program.domain.model.Program
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ProgramRepository {
    suspend fun getProgram(): Result<Program?>
    fun observeProgram(): Flow<Result<Program?>>

    suspend fun configureProgram(startDate: LocalDate, durationDays: Long): Result<Program>
}
