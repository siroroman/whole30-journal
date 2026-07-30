package dev.whole30journal.feature.dayentry.data.di

import dev.whole30journal.feature.dayentry.data.SqlDelightDayEntryRepository
import dev.whole30journal.feature.dayentry.domain.DayEntryRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dayentryDataModule = module {
    singleOf(::SqlDelightDayEntryRepository) { bind<DayEntryRepository>() }
}
