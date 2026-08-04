package dev.whole30journal.feature.dayentry.data

import app.cash.sqldelight.db.SqlDriver
import dev.whole30journal.core.database.DatabaseDriverFactory

actual fun createTestDriver(): SqlDriver = DatabaseDriverFactory().createDriver()
