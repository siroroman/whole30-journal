package dev.whole30journal.feature.program.domain.usecase

import dev.whole30journal.feature.program.domain.model.Program
import dev.whole30journal.feature.program.domain.repository.ProgramRepository

class GetProgramUseCase(
    private val repository: ProgramRepository
) {
    suspend operator fun invoke(): Result<Program?> = repository.getProgram()
}
