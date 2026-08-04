package dev.whole30journal.feature.dayentry.domain.di

import dev.whole30journal.feature.dayentry.domain.usecase.GetDayEntryUseCase
import dev.whole30journal.feature.dayentry.domain.usecase.ObserveDayEntryUseCase
import dev.whole30journal.feature.dayentry.domain.usecase.SaveDayEntryUseCase
import org.koin.dsl.module

val dayEntryDomainModule = module {
    factory { GetDayEntryUseCase(get()) }
    factory { ObserveDayEntryUseCase(get()) }
    factory { SaveDayEntryUseCase(get()) }
}
