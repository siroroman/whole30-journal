package dev.whole30journal.feature.program.domain.usecase

import dev.whole30journal.feature.program.domain.model.Program
import dev.whole30journal.feature.program.domain.repository.ProgramRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ConfigureProgramUseCase(
    private val repository: ProgramRepository,
    @OptIn(ExperimentalTime::class)
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(
        startDate: LocalDate = clock.todayIn(TimeZone.currentSystemDefault()),
        durationDays: Long = DEFAULT_DURATION_DAYS,
    ): Result<Program> = repository.configureProgram(startDate, durationDays)

    private companion object {
        const val DEFAULT_DURATION_DAYS = 30L
    }
}
