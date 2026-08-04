package dev.whole30journal.feature.program.domain.usecase

import dev.whole30journal.feature.program.domain.model.Program
import dev.whole30journal.feature.program.domain.repository.ProgramRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals

/** [ProgramRepository] is faked here rather than hit for real (unlike ProgramRepositoryImplTest in
 * program:data, which runs against a real DB) - this only needs to verify the use case's own
 * defaulting logic, not persistence. */
class ConfigureProgramUseCaseTest {

    @Test
    fun `defaults to today and a 30-day duration and returns the repository's result`() = runTest {
        val today = LocalDate(2026, 8, 4)
        val program = Program(
            startDate = today,
            durationDays = 30L,
            endDate = LocalDate(2026, 9, 2),
            currentDayNumber = 1L,
        )
        val repository = RecordingProgramRepository(resultToReturn = Result.success(program))
        val useCase = ConfigureProgramUseCase(repository, FixedClock(today))

        val result = useCase()

        assertEquals(today, repository.lastStartDate)
        assertEquals(30L, repository.lastDurationDays)
        assertEquals(Result.success(program), result)
    }

    @Test
    fun `uses the supplied startDate and durationDays instead of the defaults`() = runTest {
        val repository = RecordingProgramRepository()
        val useCase = ConfigureProgramUseCase(repository, FixedClock(today = LocalDate(2026, 8, 4)))

        useCase(startDate = LocalDate(2026, 9, 1), durationDays = 45L)

        assertEquals(LocalDate(2026, 9, 1), repository.lastStartDate)
        assertEquals(45L, repository.lastDurationDays)
    }
}

private class RecordingProgramRepository(
    private val resultToReturn: Result<Program> = Result.success(
        Program(
            startDate = LocalDate(2026, 8, 4),
            durationDays = 30L,
            endDate = LocalDate(2026, 9, 2),
            currentDayNumber = 1L,
        ),
    ),
) : ProgramRepository {
    var lastStartDate: LocalDate? = null
        private set
    var lastDurationDays: Long? = null
        private set

    override suspend fun getProgram(): Result<Program?> = error("not exercised by ConfigureProgramUseCaseTest")
    override fun observeProgram(): Flow<Result<Program?>> = error("not exercised by ConfigureProgramUseCaseTest")

    override suspend fun configureProgram(startDate: LocalDate, durationDays: Long): Result<Program> {
        lastStartDate = startDate
        lastDurationDays = durationDays
        return resultToReturn
    }
}

private class FixedClock(today: LocalDate) : Clock {
    private val instant: Instant = today.atStartOfDayIn(TimeZone.currentSystemDefault())
    override fun now(): Instant = instant
}
