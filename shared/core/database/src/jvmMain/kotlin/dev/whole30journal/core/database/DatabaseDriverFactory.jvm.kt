package dev.whole30journal.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

/** Test-only target (see build.gradle.kts) - an in-memory DB is all that's needed here. */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, schema = Whole30Database.Schema)
}
