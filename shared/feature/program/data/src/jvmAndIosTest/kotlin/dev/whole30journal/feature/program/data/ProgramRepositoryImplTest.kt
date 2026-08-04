package dev.whole30journal.feature.program.data

import app.cash.sqldelight.db.SqlDriver
import dev.whole30journal.core.database.Whole30Database
import dev.whole30journal.feature.program.domain.model.Program
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Runs against a real in-memory SQLite DB (via [createTestDriver]) rather than mocks - see
 * DayEntryRepositoryImplTest's header comment for why. Runs on the JVM and iOS targets - see
 * jvmAndIosTest in build.gradle.kts. */
class ProgramRepositoryImplTest {

    private val driver: SqlDriver = createTestDriver()
    private val database = Whole30Database(driver)

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun `getProgram returns success with null when nothing was ever configured`() = runTest {
        val result = repository(today = LocalDate(2026, 8, 4)).getProgram()

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `configureProgram then getProgram round-trips startDate and durationDays`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        val startDate = LocalDate(2026, 8, 4)

        repository.configureProgram(startDate, durationDays = 30L).getOrThrow()

        val program = repository.getProgram().getOrThrow()
        assertEquals(startDate, program?.startDate)
        assertEquals(30L, program?.durationDays)
    }

    @Test
    fun `configureProgram computes endDate as the last of durationDays days`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))

        val program = repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()

        assertEquals(LocalDate(2026, 9, 2), program.endDate) // Aug 4 + 29 days
    }

    @Test
    fun `configureProgram seeds an empty day entry for every day of the program`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        val startDate = LocalDate(2026, 8, 4)

        repository.configureProgram(startDate, durationDays = 5L).getOrThrow()

        for (dayNumber in 1L..5L) {
            val entry = database.dayEntryQueries.selectByDayNumber(dayNumber).executeAsOneOrNull()
            assertEquals(startDate.plus(dayNumber - 1, DateTimeUnit.DAY).toString(), entry?.date)
            assertEquals("", entry?.notes)
            assertEquals(0L, entry?.isComplete)
        }
    }

    @Test
    fun `configureProgram does not overwrite a day entry that already has real data`() = runTest {
        database.dayEntryQueries.upsert(dayNumber = 1L, date = "2026-08-04", notes = "Already logged", isComplete = 1L)
        val repository = repository(today = LocalDate(2026, 8, 4))

        repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 3L).getOrThrow()

        val entry = database.dayEntryQueries.selectByDayNumber(1L).executeAsOneOrNull()
        assertEquals("Already logged", entry?.notes)
        assertEquals(1L, entry?.isComplete)
    }

    @Test
    fun `reconfiguring replaces the previous program instead of adding a second row`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        repository.configureProgram(LocalDate(2026, 7, 1), durationDays = 10L).getOrThrow()

        val reconfigured = repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()

        assertEquals(reconfigured, repository.getProgram().getOrThrow())
    }

    @Test
    fun `currentDayNumber is 1 on the start date`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))

        val program = repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()

        assertEquals(1L, program.currentDayNumber)
    }

    @Test
    fun `currentDayNumber reflects elapsed days mid-program`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 9))

        val program = repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()

        assertEquals(6L, program.currentDayNumber) // Aug 4 is day 1, so Aug 9 is day 6
    }

    @Test
    fun `currentDayNumber clamps to 1 before the program has started`() = runTest {
        val repository = repository(today = LocalDate(2026, 7, 1))

        val program = repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()

        assertEquals(1L, program.currentDayNumber)
    }

    @Test
    fun `currentDayNumber clamps to durationDays after the program has ended`() = runTest {
        val repository = repository(today = LocalDate(2026, 12, 25))

        val program = repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()

        assertEquals(30L, program.currentDayNumber)
    }

    @Test
    fun `configureProgram fails for a non-positive durationDays`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))

        val result = repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 0L)

        assertTrue(result.isFailure)
        assertNull(repository.getProgram().getOrThrow())
    }

    @Test
    fun `observeProgram reflects the current state whenever it's collected`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        assertNull(repository.observeProgram().first().getOrThrow())

        val startDate = LocalDate(2026, 8, 4)
        repository.configureProgram(startDate, durationDays = 30L).getOrThrow()

        assertEquals(startDate, repository.observeProgram().first().getOrThrow()?.startDate)
    }

    @Test
    fun `observeProgram pushes a new emission when configureProgram changes the row`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        val emissions = Channel<Result<Program?>>(Channel.UNLIMITED)
        val job = launch { repository.observeProgram().collect { emissions.send(it) } }

        assertNull(emissions.receive().getOrThrow())

        repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()

        assertEquals(30L, emissions.receive().getOrThrow()?.durationDays)

        job.cancel()
    }

    private fun repository(today: LocalDate) = ProgramRepositoryImpl(database, FixedClock(today))
}

private class FixedClock(today: LocalDate) : Clock {
    private val instant: Instant = today.atStartOfDayIn(TimeZone.currentSystemDefault())
    override fun now(): Instant = instant
}
