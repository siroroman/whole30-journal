package dev.whole30journal.core.database.di

import dev.whole30journal.core.database.DatabaseDriverFactory
import org.koin.dsl.module

val iosDatabaseModule = module {
    single { DatabaseDriverFactory() }
}
