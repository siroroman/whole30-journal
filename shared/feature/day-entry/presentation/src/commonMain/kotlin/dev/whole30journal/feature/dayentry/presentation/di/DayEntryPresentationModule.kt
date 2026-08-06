package dev.whole30journal.feature.dayentry.presentation.di

import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dayEntryPresentationModule = module {
    viewModelOf(::DayEntryViewModel)
}
