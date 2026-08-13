package dev.whole30journal.feature.daydetail.presentation.di

import dev.whole30journal.feature.daydetail.presentation.vm.DayDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dayDetailPresentationModule = module {
    viewModelOf(::DayDetailViewModel)
}
