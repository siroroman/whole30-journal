package dev.whole30journal.feature.example.data

import dev.whole30journal.feature.example.domain.CatFact
import kotlinx.serialization.Serializable

@Serializable
internal data class CatFactDto(
    val fact: String,
    val length: Int,
)

internal fun CatFactDto.toDomain() = CatFact(fact = fact)
