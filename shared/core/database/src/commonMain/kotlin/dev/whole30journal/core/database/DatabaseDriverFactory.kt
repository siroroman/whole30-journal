package dev.whole30journal.core.database

import app.cash.sqldelight.db.SqlDriver

internal const val DATABASE_NAME = "whole30.db"

/**
 * Platform-specific [SqlDriver] creation - `AndroidSqliteDriver` (needs a `Context`) on Android,
 * `NativeSqliteDriver` on iOS. See the `androidMain`/`iosMain` `actual` implementations.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
