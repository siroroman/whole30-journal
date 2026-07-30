package dev.whole30journal.core.database.di

import app.cash.sqldelight.db.SqlDriver
import dev.whole30journal.core.database.DatabaseDriverFactory
import dev.whole30journal.core.database.Whole30Database
import org.koin.dsl.module

/** Platform-agnostic half of DB wiring - the [DatabaseDriverFactory] itself is bound per-platform
 * (see `androidDatabaseModule`/`iosDatabaseModule`), since it needs a `Context` on Android. */
val databaseModule = module {
    single<SqlDriver> { get<DatabaseDriverFactory>().createDriver() }
    single { Whole30Database(get()) }
}
