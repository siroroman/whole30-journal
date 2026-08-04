package dev.whole30journal.feature.program.domain.usecase

import dev.whole30journal.feature.program.domain.model.Program
import dev.whole30journal.feature.program.domain.repository.ProgramRepository
import kotlinx.coroutines.flow.Flow

class ObserveProgramUseCase(
    private val repository: ProgramRepository
) {
    operator fun invoke(): Flow<Result<Program?>> = repository.observeProgram()
}
