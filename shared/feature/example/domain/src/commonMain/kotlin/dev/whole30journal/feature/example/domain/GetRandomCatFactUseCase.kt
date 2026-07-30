package dev.whole30journal.feature.example.domain

class GetRandomCatFactUseCase(
    private val repository: CatFactRepository
) {
    suspend operator fun invoke(): Result<CatFact> = repository.getRandom()
}
