package dev.whole30journal.feature.home.presentation.di

import dev.whole30journal.core.utils.DateFormatter
import dev.whole30journal.feature.home.presentation.vm.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homePresentationModule = module {
    factory { DateFormatter() }
    viewModel { HomeViewModel(get(), get(), get()) }
}
