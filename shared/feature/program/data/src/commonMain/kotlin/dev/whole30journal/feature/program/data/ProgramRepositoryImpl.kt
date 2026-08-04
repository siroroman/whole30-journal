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

internal class ProgramRepositoryImpl(
    private val database: Whole30Database,
    private val clock: Clock = Clock.System,
) : ProgramRepository {

    override suspend fun getProgram(): Result<Program?> = runCatchingCancellable {
        withContext(dbDispatcher) { loadProgram() }
    }

    override fun observeProgram(): Flow<Result<Program?>> {
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
                    database.programQueries.deleteAll()
                    database.programQueries.insert(startDate = startDate.toString(), durationDays = durationDays)

                    for (dayNumber in 1..durationDays) {
                        val date = startDate.plus(dayNumber - 1, DateTimeUnit.DAY).toString()
                        database.dayEntryQueries.updateDate(date = date, dayNumber = dayNumber)
                        database.dayEntryQueries.insertIfAbsent(dayNumber = dayNumber, date = date)
                    }

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
