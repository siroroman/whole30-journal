@file:OptIn(ExperimentalTime::class)

package dev.whole30journal.feature.program.data

import app.cash.sqldelight.db.SqlDriver
import dev.whole30journal.core.database.Whole30Database
import dev.whole30journal.core.utils.MealPhotoStorage
import dev.whole30journal.feature.program.domain.model.Program
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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

        assertEquals(LocalDate(2026, 9, 2), program.endDate)
    }

    @Test
    fun `configureProgram seeds an empty day entry for every day of the program`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        val startDate = LocalDate(2026, 8, 4)

        repository.configureProgram(startDate, durationDays = 5L).getOrThrow()

        for (dayNumber in 0L..4L) {
            val date = startDate.plus(dayNumber, DateTimeUnit.DAY)
            val entry = database.dayEntryQueries.selectByDate(date.toString()).executeAsOneOrNull()
            assertEquals("", entry?.notes)
            assertEquals(0L, entry?.isComplete)
        }
    }

    @Test
    fun `configureProgram does not overwrite a day entry that already has real data`() = runTest {
        database.dayEntryQueries.upsert(date = "2026-08-04", notes = "Already logged", isComplete = 1L)
        val repository = repository(today = LocalDate(2026, 8, 4))

        repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 3L).getOrThrow()

        val entry = database.dayEntryQueries.selectByDate("2026-08-04").executeAsOneOrNull()
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
    fun `reconfiguring with a different startDate does not move a day entry left over from before to a new date`() =
        runTest {
            val repository = repository(today = LocalDate(2026, 8, 4))
            repository.configureProgram(LocalDate(2026, 7, 1), durationDays = 10L).getOrThrow()

            repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()

            val oldFirstDay = database.dayEntryQueries.selectByDate("2026-07-01").executeAsOneOrNull()
            assertTrue(oldFirstDay != null)
            val newFirstDay = database.dayEntryQueries.selectByDate("2026-08-04").executeAsOneOrNull()
            assertEquals("", newFirstDay?.notes)
        }

    @Test
    fun `reconfiguring with a different startDate still preserves notes on a day that already has real data`() =
        runTest {
            database.dayEntryQueries.upsert(date = "2026-07-01", notes = "Already logged", isComplete = 1L)
            val repository = repository(today = LocalDate(2026, 8, 4))

            repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()

            val entry = database.dayEntryQueries.selectByDate("2026-07-01").executeAsOneOrNull()
            assertEquals("Already logged", entry?.notes)
            assertEquals(1L, entry?.isComplete)
        }

    @Test
    fun `reconfiguring with a shorter durationDays keeps day entries beyond the new range`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        repository.configureProgram(LocalDate(2026, 7, 1), durationDays = 30L).getOrThrow()

        repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 10L).getOrThrow()

        assertTrue(database.dayEntryQueries.selectByDate("2026-07-11").executeAsOneOrNull() != null)
        assertTrue(database.dayEntryQueries.selectByDate("2026-07-30").executeAsOneOrNull() != null)
    }

    @Test
    fun `reconfiguring with a shorter durationDays keeps child rows beyond the new range`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        repository.configureProgram(LocalDate(2026, 7, 1), durationDays = 30L).getOrThrow()
        database.mealQueries.upsert(
            id = "meal-25",
            date = "2026-07-25",
            label = "Lunch",
            description = "Salad",
            photoToken = null,
            lovedIt = 0L,
            sortOrder = 0L,
        )

        repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 10L).getOrThrow()

        assertTrue(database.mealQueries.selectByDate("2026-07-25").executeAsList().isNotEmpty())
    }

    @Test
    fun `extending an existing program's durationDays keeps every existing day entry and seeds only the new days`() =
        runTest {
            val repository = repository(today = LocalDate(2026, 8, 4))
            val startDate = LocalDate(2026, 8, 4)
            repository.configureProgram(startDate, durationDays = 30L).getOrThrow()
            database.dayEntryQueries.upsert(date = "2026-08-23", notes = "Halfway there", isComplete = 1L)

            val program = repository.configureProgram(startDate, durationDays = 35L).getOrThrow()

            assertEquals(35L, program.durationDays)
            assertEquals(LocalDate(2026, 9, 7), program.endDate)

            val day20 = database.dayEntryQueries.selectByDate("2026-08-23").executeAsOneOrNull()
            assertEquals("Halfway there", day20?.notes)
            assertEquals(1L, day20?.isComplete)

            for (dayNumber in 30L..34L) {
                val date = startDate.plus(dayNumber, DateTimeUnit.DAY)
                val entry = database.dayEntryQueries.selectByDate(date.toString()).executeAsOneOrNull()
                assertEquals("", entry?.notes)
                assertEquals(0L, entry?.isComplete)
            }
        }

    @Test
    fun `shortening an existing program's durationDays keeps the day entries it trims off even with real data`() =
        runTest {
            val repository = repository(today = LocalDate(2026, 8, 4))
            val startDate = LocalDate(2026, 8, 4)
            repository.configureProgram(startDate, durationDays = 30L).getOrThrow()
            database.dayEntryQueries.upsert(date = "2026-08-31", notes = "Cheat day, oops", isComplete = 1L)

            val program = repository.configureProgram(startDate, durationDays = 25L).getOrThrow()

            assertEquals(25L, program.durationDays)
            assertEquals(LocalDate(2026, 8, 28), program.endDate)

            assertTrue(database.dayEntryQueries.selectByDate("2026-08-28").executeAsOneOrNull() != null)
            val trimmedDay = database.dayEntryQueries.selectByDate("2026-08-31").executeAsOneOrNull()
            assertEquals("Cheat day, oops", trimmedDay?.notes)
            assertEquals(1L, trimmedDay?.isComplete)
        }

    @Test
    fun `shifting an existing program's startDate keeps a logged day entry on its original calendar date`() =
        runTest {
            val repository = repository(today = LocalDate(2026, 8, 4))
            repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 30L).getOrThrow()
            database.dayEntryQueries.upsert(date = "2026-08-13", notes = "Felt great", isComplete = 1L)

            val newStartDate = LocalDate(2026, 8, 6)
            val program = repository.configureProgram(newStartDate, durationDays = 30L).getOrThrow()

            assertEquals(newStartDate, program.startDate)
            assertEquals(LocalDate(2026, 9, 4), program.endDate)

            val newFirstDay = database.dayEntryQueries.selectByDate("2026-08-06").executeAsOneOrNull()
            assertEquals("", newFirstDay?.notes)

            val loggedDay = database.dayEntryQueries.selectByDate("2026-08-13").executeAsOneOrNull()
            assertEquals("Felt great", loggedDay?.notes)
            assertEquals(1L, loggedDay?.isComplete)
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

        assertEquals(6L, program.currentDayNumber)
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
    fun `deleteAllData clears the program and every child table`() = runTest {
        val repository = repository(today = LocalDate(2026, 8, 4))
        repository.configureProgram(LocalDate(2026, 8, 4), durationDays = 5L).getOrThrow()
        database.metricQueries.upsert(date = "2026-08-04", title = "Energy", iconName = "bolt", value_ = 3L, maxValue = 5L, note = "")
        database.mealQueries.upsert(
            id = "meal-1",
            date = "2026-08-04",
            label = "Lunch",
            description = "Salad",
            photoToken = null,
            lovedIt = 0L,
            sortOrder = 0L,
        )
        database.achievementQueries.upsert(id = "achievement-1", date = "2026-08-04", text = "Felt great", sortOrder = 0L)

        repository.deleteAllData().getOrThrow()

        assertNull(repository.getProgram().getOrThrow())
        assertNull(database.dayEntryQueries.selectByDate("2026-08-04").executeAsOneOrNull())
        assertTrue(database.metricQueries.selectByDate("2026-08-04").executeAsList().isEmpty())
        assertTrue(database.mealQueries.selectByDate("2026-08-04").executeAsList().isEmpty())
        assertTrue(database.achievementQueries.selectByDate("2026-08-04").executeAsList().isEmpty())
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

    @Test
    fun `moving the start date earlier leaves data logged for day 1 on its original calendar date`() = runTest {
        val repository = repository(today = LocalDate(2026, 9, 16))
        repository.configureProgram(LocalDate(2026, 9, 16), durationDays = 30L).getOrThrow()
        database.dayEntryQueries.upsert(date = "2026-09-16", notes = "Day one notes", isComplete = 1L)

        repository.configureProgram(LocalDate(2026, 9, 14), durationDays = 30L).getOrThrow()

        val newDayOne = database.dayEntryQueries.selectByDate("2026-09-14").executeAsOneOrNull()
        assertEquals("", newDayOne?.notes)
        assertEquals(0L, newDayOne?.isComplete)

        val originalEntry = database.dayEntryQueries.selectByDate("2026-09-16").executeAsOneOrNull()
        assertEquals("Day one notes", originalEntry?.notes)
        assertEquals(1L, originalEntry?.isComplete)
    }

    private fun repository(today: LocalDate) = ProgramRepositoryImpl(database, MealPhotoStorage(), FixedClock(today))
}

private class FixedClock(today: LocalDate) : Clock {
    private val instant: Instant = today.atStartOfDayIn(TimeZone.currentSystemDefault())
    override fun now(): Instant = instant
}
