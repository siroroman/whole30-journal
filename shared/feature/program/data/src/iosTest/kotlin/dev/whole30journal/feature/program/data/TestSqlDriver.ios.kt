package dev.whole30journal.feature.program.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import dev.whole30journal.core.database.Whole30Database

actual fun createTestDriver(): SqlDriver = inMemoryDriver(Whole30Database.Schema)
