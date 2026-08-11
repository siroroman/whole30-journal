package dev.whole30journal.umbrella.di

import dev.whole30journal.core.database.di.databaseModule
import dev.whole30journal.core.network.di.networkModule
import dev.whole30journal.feature.dayentry.data.di.dayEntryDataModule
import dev.whole30journal.feature.dayentry.domain.di.dayEntryDomainModule
import dev.whole30journal.feature.dayentry.presentation.di.dayEntryPresentationModule
import dev.whole30journal.feature.home.presentation.di.homePresentationModule
import dev.whole30journal.feature.program.data.di.programDataModule
import dev.whole30journal.feature.program.domain.di.programDomainModule
import org.koin.core.module.Module

/** All Koin modules the app needs, combined once so Android and iOS bootstrap identically. Each
 * platform additionally supplies its own [dev.whole30journal.core.database.DatabaseDriverFactory]
 * binding (`androidDatabaseModule`/`iosDatabaseModule`) since that needs a `Context` on Android. */
val appModules: List<Module> = listOf(
    networkModule,
    databaseModule,
    dayEntryDomainModule,
    dayEntryDataModule,
    programDomainModule,
    programDataModule,
    homePresentationModule,
    dayEntryPresentationModule,
)
