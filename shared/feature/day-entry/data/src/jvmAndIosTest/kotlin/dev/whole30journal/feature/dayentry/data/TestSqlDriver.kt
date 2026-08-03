package dev.whole30journal.feature.dayentry.data

import app.cash.sqldelight.db.SqlDriver

/** A fresh, isolated in-memory [SqlDriver] - unlike [dev.whole30journal.core.database.DatabaseDriverFactory],
 * which on iOS opens the real on-disk app database, so it can't be reused here without leaking state
 * across tests. */
expect fun createTestDriver(): SqlDriver
