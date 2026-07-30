package dev.whole30journal.feature.example.data

import dev.whole30journal.core.network.NetworkClient
import dev.whole30journal.feature.example.domain.CatFact
import dev.whole30journal.feature.example.domain.CatFactRepository
import io.ktor.client.HttpClient

/** Talks to the [Cat Facts API](https://catfact.ninja/#/Facts), a free public REST API. */
internal class CatFactRepositoryImpl(
    httpClient: HttpClient,
) : NetworkClient(httpClient), CatFactRepository {

    override suspend fun getRandom(): Result<CatFact> =
        get<CatFactDto>("$BASE_URL/fact").map { it.toDomain() }

    private companion object {
        const val BASE_URL = "https://catfact.ninja"
    }
}
