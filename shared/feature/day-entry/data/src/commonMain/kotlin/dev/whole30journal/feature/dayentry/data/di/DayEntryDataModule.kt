package dev.whole30journal.feature.dayentry.data.di

import dev.whole30journal.feature.dayentry.data.DayEntryRepositoryImpl
import dev.whole30journal.feature.dayentry.domain.repository.DayEntryRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dayentryDataModule = module {
    singleOf(::DayEntryRepositoryImpl) { bind<DayEntryRepository>() }
}
