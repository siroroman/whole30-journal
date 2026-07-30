package dev.whole30journal.feature.example.presentation.di

import dev.whole30journal.feature.example.presentation.vm.ExampleViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val examplePresentationModule = module {
    viewModelOf(::ExampleViewModel)
}
