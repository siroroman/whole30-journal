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
    fun `reconfiguring with a different startDate refreshes the date of a day entry left over from before`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        repository.configureProgram(LocalDate(2026, 7, 1), durationDays = 10L).getOrThrow()

        repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()

        val entry = database.dayEntryQueries.selectByDayNumber(1L).executeAsOneOrNull()
        assertEquals("2026-08-04", entry?.date)
    }

    @Test
    fun `reconfiguring with a different startDate still preserves notes on a day that already has real data`() = runTest {
        database.dayEntryQueries.upsert(dayNumber = 1L, date = "2026-07-01", notes = "Already logged", isComplete = 1L)
        val repository = repository(today = LocalDate(2026, 8, 4))

        repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()

        val entry = database.dayEntryQueries.selectByDayNumber(1L).executeAsOneOrNull()
        assertEquals("2026-08-04", entry?.date)
        assertEquals("Already logged", entry?.notes)
        assertEquals(1L, entry?.isComplete)
    }

    @Test
    fun `reconfiguring with a shorter durationDays removes day entries beyond the new range`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        repository.configureProgram(LocalDate(2026, 7, 1), durationDays = 30L).getOrThrow()

        repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 10L).getOrThrow()

        assertNull(database.dayEntryQueries.selectByDayNumber(11L).executeAsOneOrNull())
        assertNull(database.dayEntryQueries.selectByDayNumber(30L).executeAsOneOrNull())
    }

    @Test
    fun `reconfiguring with a shorter durationDays also removes child rows beyond the new range`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        repository.configureProgram(LocalDate(2026, 7, 1), durationDays = 30L).getOrThrow()
        database.mealQueries.upsert(
            id = "meal-25",
            dayNumber = 25L,
            label = "Lunch",
            description = "Salad",
            photoToken = null,
            lovedIt = 0L,
            sortOrder = 0L,
        )

        repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 10L).getOrThrow()

        assertTrue(database.mealQueries.selectByDayNumber(25L).executeAsList().isEmpty())
    }

    // Real-world reconfigure scenarios: a user adjusting an in-progress program rather than
    // starting a fresh one. Each asserts both the Program the change produces and what happens to
    // the underlying day entries - including any real data already logged against them.

    @Test
    fun `extending an existing program's durationDays keeps every existing day entry and seeds only the new days`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        val startDate = LocalDate(2026, 8, 4)
        repository.configureProgram(startDate, durationDays = 30L).getOrThrow()
        // Day 20 already has real user data from before the extension.
        database.dayEntryQueries.upsert(dayNumber = 20L, date = "2026-08-23", notes = "Halfway there", isComplete = 1L)

        // Extend by 5 days (30 -> 35) without changing startDate.
        val program = repository.configureProgram(startDate, durationDays = 35L).getOrThrow()

        assertEquals(35L, program.durationDays)
        assertEquals(LocalDate(2026, 9, 7), program.endDate) // Aug 4 + 34 days

        // Day 20's date and logged data are both untouched - its calendar date didn't change.
        val day20 = database.dayEntryQueries.selectByDayNumber(20L).executeAsOneOrNull()
        assertEquals("2026-08-23", day20?.date)
        assertEquals("Halfway there", day20?.notes)
        assertEquals(1L, day20?.isComplete)

        // Days 31-35 are newly seeded with the correct dates and empty data.
        for (dayNumber in 31L..35L) {
            val entry = database.dayEntryQueries.selectByDayNumber(dayNumber).executeAsOneOrNull()
            assertEquals(startDate.plus(dayNumber - 1, DateTimeUnit.DAY).toString(), entry?.date)
            assertEquals("", entry?.notes)
            assertEquals(0L, entry?.isComplete)
        }
    }

    @Test
    fun `shortening an existing program's durationDays discards the day entries it trims off, even with real data`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        val startDate = LocalDate(2026, 8, 4)
        repository.configureProgram(startDate, durationDays = 30L).getOrThrow()
        // Day 28 was already logged - shortening the program to end before it deletes it anyway,
        // since a day past the new end date no longer belongs to any program.
        database.dayEntryQueries.upsert(dayNumber = 28L, date = "2026-08-31", notes = "Cheat day, oops", isComplete = 1L)

        // Shorten by 5 days (30 -> 25) without changing startDate.
        val program = repository.configureProgram(startDate, durationDays = 25L).getOrThrow()

        assertEquals(25L, program.durationDays)
        assertEquals(LocalDate(2026, 8, 28), program.endDate) // Aug 4 + 24 days

        // Days within the new range are untouched.
        assertTrue(database.dayEntryQueries.selectByDayNumber(25L).executeAsOneOrNull() != null)
        // Days 26-30 - including day 28's logged data - are gone.
        for (dayNumber in 26L..30L) {
            assertNull(database.dayEntryQueries.selectByDayNumber(dayNumber).executeAsOneOrNull())
        }
    }

    @Test
    fun `shifting an existing program's startDate by two days re-dates every existing day entry but keeps its data`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()
        // Day 10 already has real user data logged against the old startDate's mapping.
        database.dayEntryQueries.upsert(dayNumber = 10L, date = "2026-08-13", notes = "Felt great", isComplete = 1L)

        // Push the start date back by two days (Aug 4 -> Aug 6), same durationDays.
        val newStartDate = LocalDate(2026, 8, 6)
        val program = repository.configureProgram(newStartDate, durationDays = 30L).getOrThrow()

        assertEquals(newStartDate, program.startDate)
        assertEquals(LocalDate(2026, 9, 4), program.endDate) // Aug 6 + 29 days

        // Day 1 now maps to the new startDate itself.
        val day1 = database.dayEntryQueries.selectByDayNumber(1L).executeAsOneOrNull()
        assertEquals("2026-08-06", day1?.date)

        // Day 10's date shifts by the same two days as every other day, but its logged data survives
        // the shift - only the date column is re-synced, notes/isComplete are never touched.
        val day10 = database.dayEntryQueries.selectByDayNumber(10L).executeAsOneOrNull()
        assertEquals("2026-08-15", day10?.date) // was Aug 13, now Aug 6 + 9 days
        assertEquals("Felt great", day10?.notes)
        assertEquals(1L, day10?.isComplete)

        // No day entries are added or removed - the day count didn't change, only the mapping did.
        assertTrue(database.dayEntryQueries.selectByDayNumber(30L).executeAsOneOrNull() != null)
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
