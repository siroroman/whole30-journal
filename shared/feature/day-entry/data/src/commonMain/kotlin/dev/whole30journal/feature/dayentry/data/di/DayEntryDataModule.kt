package dev.whole30journal.feature.dayentry.data.di

import dev.whole30journal.feature.dayentry.data.DayEntryRepositoryImpl
import dev.whole30journal.feature.dayentry.domain.repository.DayEntryRepository
import org.koin.dsl.module

val dayEntryDataModule = module {
    single<DayEntryRepository> { DayEntryRepositoryImpl(get()) }
}
