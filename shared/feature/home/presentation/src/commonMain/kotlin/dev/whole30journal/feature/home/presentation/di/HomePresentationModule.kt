package dev.whole30journal.feature.home.presentation.di

import dev.whole30journal.feature.home.presentation.vm.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homePresentationModule = module {
    viewModelOf(::HomeViewModel)
}
