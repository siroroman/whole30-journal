package dev.whole30journal.feature.example.domain.di

import dev.whole30journal.feature.example.domain.GetRandomCatFactUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val exampleDomainModule = module {
    factoryOf(::GetRandomCatFactUseCase)
}
