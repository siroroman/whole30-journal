package dev.whole30journal.feature.program.domain.usecase

import dev.whole30journal.feature.program.domain.repository.ProgramRepository

class DeleteAllDataUseCase(
    private val repository: ProgramRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.deleteAllData()
}
