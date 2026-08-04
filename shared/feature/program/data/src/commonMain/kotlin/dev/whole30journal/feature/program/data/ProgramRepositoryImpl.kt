package dev.whole30journal.feature.program.data

import app.cash.sqldelight.coroutines.asFlow
import dev.whole30journal.core.database.Whole30Database
import dev.whole30journal.core.database.runCatchingCancellable
import dev.whole30journal.feature.program.domain.model.Program
import dev.whole30journal.feature.program.domain.repository.ProgramRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import dev.whole30journal.core.database.databaseDispatcher as dbDispatcher

/** SQLDelight-backed [ProgramRepository] - the program config is a single-row `ProgramEntity`.
 * [Program.endDate]/[Program.currentDayNumber] are derived from it plus [clock] at read time rather
 * than stored, so a fresh call to [getProgram] is never stale from whatever day the row happened to
 * be written on. [observeProgram] only recomputes on a row write though - see its own doc. */
internal class ProgramRepositoryImpl(
    private val database: Whole30Database,
    private val clock: Clock = Clock.System,
) : ProgramRepository {

    override suspend fun getProgram(): Result<Program?> = runCatchingCancellable {
        withContext(dbDispatcher) { loadProgram() }
    }

    // currentDayNumber is only recomputed when ProgramEntity is written to, not on a timer - a
    // long-lived collector won't see it tick over at midnight on its own, only on the next write.
    override fun observeProgram(): Flow<Result<Program?>> {
        // select() emits once immediately on subscribe; drop() that synthetic first emission and
        // add back exactly one via onStart, matching DayEntryRepositoryImpl's observe pattern.
        val invalidations = database.programQueries.select().asFlow().map { }.drop(1).onStart { emit(Unit) }
        return invalidations
            .conflate()
            .map { withContext(dbDispatcher) { loadProgram() } }
            .distinctUntilChanged()
            .map { Result.success(it) }
            .catch { e ->
                if (e is CancellationException) throw e
                emit(Result.failure(e))
            }
    }

    override suspend fun configureProgram(startDate: LocalDate, durationDays: Long): Result<Program> =
        runCatchingCancellable {
            require(durationDays > 0) { "durationDays must be positive, was $durationDays" }
            withContext(dbDispatcher) {
                database.programQueries.transaction {
                    // The program config itself is a full replace (there's only ever one)...
                    database.programQueries.deleteAll()
                    database.programQueries.insert(startDate = startDate.toString(), durationDays = durationDays)

                    // ...but seeding day entries only refreshes each day's date and fills in a fresh
                    // row where one is still missing, so reconfiguring never wipes a day that already
                    // has real user data (notes, completion, or child rows) behind it - it just keeps
                    // that day's date in sync with whichever program now owns dayNumber.
                    for (dayNumber in 1..durationDays) {
                        val date = startDate.plus(dayNumber - 1, DateTimeUnit.DAY).toString()
                        database.dayEntryQueries.updateDate(date = date, dayNumber = dayNumber)
                        database.dayEntryQueries.insertIfAbsent(dayNumber = dayNumber, date = date)
                    }

                    // Day numbers beyond the new duration belonged to a previous, longer-or-differently
                    // -dated configuration and no longer correspond to any day of the current program -
                    // clean them up (children first, parent last) instead of leaving them orphaned.
                    database.metricQueries.deleteAfterDayNumber(durationDays)
                    database.mealQueries.deleteAfterDayNumber(durationDays)
                    database.achievementQueries.deleteAfterDayNumber(durationDays)
                    database.dayEntryQueries.deleteAfterDayNumber(durationDays)
                }
            }
            buildProgram(startDate, durationDays, today = clock.todayIn(TimeZone.currentSystemDefault()))
        }

    private fun loadProgram(): Program? {
        val entity = database.programQueries.select().executeAsOneOrNull() ?: return null
        return buildProgram(
            startDate = LocalDate.parse(entity.startDate),
            durationDays = entity.durationDays,
            today = clock.todayIn(TimeZone.currentSystemDefault()),
        )
    }
}

// currentDayNumber is clamped to [1, durationDays] so it's always a valid day-entry index, even
// before the program has started or after its last day has passed.
private fun buildProgram(startDate: LocalDate, durationDays: Long, today: LocalDate): Program {
    val endDate = startDate.plus(durationDays - 1, DateTimeUnit.DAY)
    val currentDayNumber = (startDate.daysUntil(today) + 1).toLong().coerceIn(1L, durationDays)
    return Program(
        startDate = startDate,
        durationDays = durationDays,
        endDate = endDate,
        currentDayNumber = currentDayNumber,
    )
}
