package dev.whole30journal.umbrella.di

import dev.whole30journal.core.network.di.networkModule
import dev.whole30journal.feature.example.data.di.exampleDataModule
import dev.whole30journal.feature.example.domain.di.exampleDomainModule
import dev.whole30journal.feature.example.presentation.di.examplePresentationModule
import org.koin.core.module.Module

/** All Koin modules the app needs, combined once so Android and iOS bootstrap identically. */
val appModules: List<Module> = listOf(
    networkModule,
    exampleDomainModule,
    exampleDataModule,
    examplePresentationModule,
)
