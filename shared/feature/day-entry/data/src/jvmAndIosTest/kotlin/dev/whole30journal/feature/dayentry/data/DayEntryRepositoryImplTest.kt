package dev.whole30journal.feature.dayentry.data

import app.cash.sqldelight.db.SqlDriver
import dev.whole30journal.core.database.Whole30Database
import dev.whole30journal.feature.dayentry.domain.model.Achievement
import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import dev.whole30journal.feature.dayentry.domain.model.Meal
import dev.whole30journal.feature.dayentry.domain.model.Metric
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Runs against a real in-memory SQLite DB (via [createTestDriver]) rather than mocks, since the
 * behaviour worth verifying here is the SQL/mapping/transaction logic itself. Runs on the JVM and
 * iOS targets - see jvmAndIosTest in build.gradle.kts. */
class DayEntryRepositoryImplTest {

    private val driver: SqlDriver = createTestDriver()
    private val repository = DayEntryRepositoryImpl(Whole30Database(driver))

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun `getDayEntry returns success with null when nothing was ever saved`() = runTest {
        val result = repository.getDayEntry(dayNumber = 1L)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `saveDayEntry then getDayEntry round-trips the full aggregate`() = runTest {
        val entry = sampleDayEntry(dayNumber = 1L)

        repository.saveDayEntry(entry).getOrThrow()

        assertEquals(entry, repository.getDayEntry(1L).getOrThrow())
    }

    @Test
    fun `saveDayEntry keeps entries for different day numbers independent`() = runTest {
        val day1 = sampleDayEntry(dayNumber = 1L)
        val day2 = sampleDayEntry(dayNumber = 2L)

        repository.saveDayEntry(day1).getOrThrow()
        repository.saveDayEntry(day2).getOrThrow()

        assertEquals(day1, repository.getDayEntry(1L).getOrThrow())
        assertEquals(day2, repository.getDayEntry(2L).getOrThrow())
    }

    @Test
    fun `saveDayEntry replaces previously saved metrics meals and achievements`() = runTest {
        val original = sampleDayEntry(dayNumber = 1L)
        repository.saveDayEntry(original).getOrThrow()

        val updated = original.copy(
            notes = "Felt great today",
            isComplete = true,
            metrics = original.metrics.drop(1),
            meals = original.meals.take(1),
            achievements = emptyList(),
        )
        repository.saveDayEntry(updated).getOrThrow()

        assertEquals(updated, repository.getDayEntry(1L).getOrThrow())
    }

    @Test
    fun `observeDayEntry reflects the current state whenever it's collected`() = runTest {
        assertNull(repository.observeDayEntry(1L).first().getOrThrow())

        val entry = sampleDayEntry(dayNumber = 1L)
        repository.saveDayEntry(entry).getOrThrow()

        assertEquals(entry, repository.observeDayEntry(1L).first().getOrThrow())
    }

    @Test
    fun `observeDayEntry pushes a new emission when saveDayEntry changes the row`() = runTest {
        val emissions = Channel<Result<DayEntry?>>(Channel.UNLIMITED)
        val job = launch { repository.observeDayEntry(1L).collect { emissions.send(it) } }

        assertNull(emissions.receive().getOrThrow())

        val entry = sampleDayEntry(dayNumber = 1L)
        repository.saveDayEntry(entry).getOrThrow()

        assertEquals(entry, emissions.receive().getOrThrow())

        job.cancel()
    }

    @Test
    fun `concurrent saves and reads never observe a torn intermediate state`() = runTest {
        val versionA = sampleDayEntry(dayNumber = 1L)
        val versionB = versionA.copy(
            notes = "Version B",
            metrics = versionA.metrics.map { it.copy(note = "B") },
            meals = versionA.meals.map { it.copy(label = "${it.label} B") },
            achievements = versionA.achievements.map { it.copy(text = "${it.text} B") },
        )
        fun isConsistent(entry: DayEntry?) = entry == null || entry == versionA || entry == versionB

        val observed = mutableListOf<DayEntry?>()
        val observeJob = launch {
            repository.observeDayEntry(1L).collect { observed.add(it.getOrThrow()) }
        }

        val saveJob = launch {
            repeat(30) { i -> repository.saveDayEntry(if (i % 2 == 0) versionA else versionB).getOrThrow() }
        }
        val readJob = launch {
            repeat(30) { assertTrue(isConsistent(repository.getDayEntry(1L).getOrThrow())) }
        }
        saveJob.join()
        readJob.join()
        observeJob.cancel()

        assertTrue(observed.isNotEmpty())
        observed.forEach { assertTrue(isConsistent(it)) }
    }
}

private fun sampleDayEntry(dayNumber: Long) = DayEntry(
    dayNumber = dayNumber,
    date = "2026-07-$dayNumber",
    metrics = listOf(
        Metric(title = "Energy", iconName = "bolt", value = 4L, maxValue = 5L, note = "Felt good"),
        Metric(title = "Sleep", iconName = "moon", value = null, maxValue = 5L, note = ""),
    ),
    notes = "Stuck to the plan",
    isComplete = false,
    meals = listOf(
        Meal(
            id = "meal-$dayNumber-1",
            label = "Breakfast",
            description = "Eggs and avocado",
            photoToken = null,
            lovedIt = true,
            sortOrder = 0L,
        ),
        Meal(
            id = "meal-$dayNumber-2",
            label = "Lunch",
            description = "Chicken salad",
            photoToken = "token-abc",
            lovedIt = false,
            sortOrder = 1L,
        ),
    ),
    achievements = listOf(
        Achievement(id = "ach-$dayNumber-1", text = "No sugar cravings", sortOrder = 0L),
    ),
)
