package dev.whole30journal.umbrella.di

import dev.whole30journal.core.network.di.networkModule
import org.koin.core.module.Module

/** All Koin modules the app needs, combined once so Android and iOS bootstrap identically. */
val appModules: List<Module> = listOf(
    networkModule,
)
