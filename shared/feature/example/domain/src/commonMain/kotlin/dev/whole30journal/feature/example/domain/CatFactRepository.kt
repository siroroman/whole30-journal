package dev.whole30journal.feature.example.domain

interface CatFactRepository {
    suspend fun getRandom(): Result<CatFact>
}
