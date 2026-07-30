package dev.whole30journal.feature.dayentry.domain.di

import dev.whole30journal.feature.dayentry.domain.GetDayEntryUseCase
import dev.whole30journal.feature.dayentry.domain.ObserveDayEntryUseCase
import dev.whole30journal.feature.dayentry.domain.SaveDayEntryUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dayentryDomainModule = module {
    factoryOf(::GetDayEntryUseCase)
    factoryOf(::ObserveDayEntryUseCase)
    factoryOf(::SaveDayEntryUseCase)
}
