package dev.whole30journal.feature.example.data.di

import dev.whole30journal.feature.example.data.CatFactRepositoryImpl
import dev.whole30journal.feature.example.domain.CatFactRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val exampleDataModule = module {
    singleOf(::CatFactRepositoryImpl) { bind<CatFactRepository>() }
}
