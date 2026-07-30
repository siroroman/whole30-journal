package dev.whole30journal.feature.dayentry.domain.di

import dev.whole30journal.feature.dayentry.domain.usecase.GetDayEntryUseCase
import dev.whole30journal.feature.dayentry.domain.usecase.ObserveDayEntryUseCase
import dev.whole30journal.feature.dayentry.domain.usecase.SaveDayEntryUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dayentryDomainModule = module {
    factoryOf(::GetDayEntryUseCase)
    factoryOf(::ObserveDayEntryUseCase)
    factoryOf(::SaveDayEntryUseCase)
}
