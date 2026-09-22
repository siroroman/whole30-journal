package dev.whole30journal.feature.dayentry.data

import app.cash.sqldelight.db.SqlDriver
import dev.whole30journal.core.database.Whole30Database
import dev.whole30journal.feature.dayentry.domain.model.Achievement
import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import dev.whole30journal.feature.dayentry.domain.model.Meal
import dev.whole30journal.feature.dayentry.domain.model.Metric
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

// runTest's default 60s wall-clock timeout can be too tight for these DB-backed tests under CI's
// parallel Gradle execution on constrained runners, where the real dbDispatcher thread can be
// starved rather than the coroutine itself being stuck - see the "Fix flaky observeDayEntry" and
// "Use TestScope.backgroundScope" commits for prior (insufficient) attempts at this same flake.
// 2 minutes still wasn't enough headroom under CI contention (observeDayEntry pushes a new
// emission... failed with UncompletedCoroutinesError twice in a row on PR #18); the "Kotlin tests"
// job has a 20-minute budget and this suite finishes in ~4-5 minutes, so there's plenty of room.
private val DB_TEST_TIMEOUT = 5.minutes

/** Runs against a real in-memory SQLite DB (via [createTestDriver]) rather than mocks, since the
 * behaviour worth verifying here is the SQL/mapping/transaction logic itself. Runs on the JVM and
 * iOS targets - see jvmAndIosTest in build.gradle.kts. */
class DayEntryRepositoryImplTest {

    private val driver: SqlDriver = createTestDriver()
    private val repository = DayEntryRepositoryImpl(Whole30Database(driver))

    private val day1 = LocalDate(2026, 7, 1)
    private val day2 = LocalDate(2026, 7, 2)

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun `getDayEntry returns success with null when nothing was ever saved`() = runTest(timeout = DB_TEST_TIMEOUT) {
        val result = repository.getDayEntry(day1)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `saveDayEntry then getDayEntry round-trips the full aggregate`() = runTest(timeout = DB_TEST_TIMEOUT) {
        val entry = sampleDayEntry(day1)

        repository.saveDayEntry(entry).getOrThrow()

        assertEquals(entry, repository.getDayEntry(day1).getOrThrow())
    }

    @Test
    fun `saveDayEntry keeps entries for different dates independent`() = runTest(timeout = DB_TEST_TIMEOUT) {
        val entry1 = sampleDayEntry(day1)
        val entry2 = sampleDayEntry(day2)

        repository.saveDayEntry(entry1).getOrThrow()
        repository.saveDayEntry(entry2).getOrThrow()

        assertEquals(entry1, repository.getDayEntry(day1).getOrThrow())
        assertEquals(entry2, repository.getDayEntry(day2).getOrThrow())
    }

    @Test
    fun `saveDayEntry replaces previously saved metrics meals and achievements`() = runTest(timeout = DB_TEST_TIMEOUT) {
        val original = sampleDayEntry(day1)
        repository.saveDayEntry(original).getOrThrow()

        val updated = original.copy(
            notes = "Felt great today",
            isComplete = true,
            metrics = original.metrics.drop(1),
            meals = original.meals.take(1),
            achievements = emptyList(),
        )
        repository.saveDayEntry(updated).getOrThrow()

        assertEquals(updated, repository.getDayEntry(day1).getOrThrow())
    }

    @Test
    fun `observeDayEntry reflects the current state whenever it's collected`() = runTest(timeout = DB_TEST_TIMEOUT) {
        assertNull(repository.observeDayEntry(day1).first().getOrThrow())

        val entry = sampleDayEntry(day1)
        repository.saveDayEntry(entry).getOrThrow()

        assertEquals(entry, repository.observeDayEntry(day1).first().getOrThrow())
    }

    @Test
    fun `observeDayEntry pushes a new emission when saveDayEntry changes the row`() = runTest(timeout = DB_TEST_TIMEOUT) {
        val emissions = Channel<Result<DayEntry?>>(Channel.UNLIMITED)
        backgroundScope.launch { repository.observeDayEntry(day1).collect { emissions.send(it) } }

        assertNull(emissions.receive().getOrThrow())

        val entry = sampleDayEntry(day1)
        repository.saveDayEntry(entry).getOrThrow()

        assertEquals(entry, emissions.receive().getOrThrow())
    }

    @Test
    fun `concurrent saves and reads never observe a torn intermediate state`() = runTest(timeout = DB_TEST_TIMEOUT) {
        val versionA = sampleDayEntry(day1)
        val versionB = versionA.copy(
            notes = "Version B",
            metrics = versionA.metrics.map { it.copy(note = "B") },
            meals = versionA.meals.map { it.copy(label = "${it.label} B") },
            achievements = versionA.achievements.map { it.copy(text = "${it.text} B") },
        )
        fun isConsistent(entry: DayEntry?) = entry == null || entry == versionA || entry == versionB

        val observed = mutableListOf<DayEntry?>()
        val observeJob = backgroundScope.launch {
            repository.observeDayEntry(day1).collect { observed.add(it.getOrThrow()) }
        }

        val saveJob = launch {
            repeat(30) { i -> repository.saveDayEntry(if (i % 2 == 0) versionA else versionB).getOrThrow() }
        }
        val readJob = launch {
            repeat(30) { assertTrue(isConsistent(repository.getDayEntry(day1).getOrThrow())) }
        }
        saveJob.join()
        readJob.join()
        observeJob.cancelAndJoin()

        assertTrue(observed.isNotEmpty())
        observed.forEach { assertTrue(isConsistent(it)) }
    }
}

private fun sampleDayEntry(date: LocalDate) = DayEntry(
    date = date,
    metrics = listOf(
        Metric(title = "Energy", iconName = "bolt", value = 4L, maxValue = 5L, note = "Felt good"),
        Metric(title = "Sleep", iconName = "moon", value = null, maxValue = 5L, note = ""),
    ),
    notes = "Stuck to the plan",
    isComplete = false,
    meals = listOf(
        Meal(
            id = "meal-$date-1",
            label = "Breakfast",
            mealDescription = "Eggs and avocado",
            photoToken = null,
            lovedIt = true,
            sortOrder = 0L,
        ),
        Meal(
            id = "meal-$date-2",
            label = "Lunch",
            mealDescription = "Chicken salad",
            photoToken = "token-abc",
            lovedIt = false,
            sortOrder = 1L,
        ),
    ),
    achievements = listOf(
        Achievement(id = "ach-$date-1", text = "No sugar cravings", sortOrder = 0L),
    ),
)
