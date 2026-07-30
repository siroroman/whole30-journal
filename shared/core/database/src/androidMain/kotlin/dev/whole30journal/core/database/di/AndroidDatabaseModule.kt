package dev.whole30journal.core.database.di

import dev.whole30journal.core.database.DatabaseDriverFactory
import org.koin.dsl.module

/** Requires `Context` to already be registered - call `androidContext(this)` in `startKoin {}`. */
val androidDatabaseModule = module {
    single { DatabaseDriverFactory(get()) }
}
