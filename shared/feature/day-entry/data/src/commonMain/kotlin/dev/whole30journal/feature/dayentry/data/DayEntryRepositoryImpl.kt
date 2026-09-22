package dev.whole30journal.feature.dayentry.data

import app.cash.sqldelight.coroutines.asFlow
import dev.whole30journal.core.database.AchievementEntity
import dev.whole30journal.core.database.DayEntryEntity
import dev.whole30journal.core.database.MealEntity
import dev.whole30journal.core.database.MetricEntity
import dev.whole30journal.core.database.Whole30Database
import dev.whole30journal.core.database.runCatchingCancellable
import dev.whole30journal.feature.dayentry.domain.model.Achievement
import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import dev.whole30journal.feature.dayentry.domain.model.Meal
import dev.whole30journal.feature.dayentry.domain.model.Metric
import dev.whole30journal.feature.dayentry.domain.repository.DayEntryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import dev.whole30journal.core.database.databaseDispatcher as dbDispatcher

/** SQLDelight-backed [DayEntryRepository] - a day entry is stored as one row in `DayEntryEntity`
 * plus its child metric/meal/achievement rows, all keyed by [DayEntry.date]. */
internal class DayEntryRepositoryImpl(
    private val database: Whole30Database,
) : DayEntryRepository {

    override suspend fun getDayEntry(date: LocalDate): Result<DayEntry?> = runCatchingCancellable {
        withContext(dbDispatcher) { loadDayEntry(date) }
    }

    // A save touches all 4 tables in one transaction; combine()-ing 4 independently-invalidated
    // per-table flows would let a collector observe a torn intermediate state (e.g. the day row
    // updated but children flows still on their last-cached value). Merging into a single "something
    // changed" signal and re-reading everything fresh on each tick keeps every emission consistent.
    override fun observeDayEntry(date: LocalDate): Flow<Result<DayEntry?>> {
        val dateString = date.toString()
        // Each asFlow() source emits once immediately on subscribe, so merging all 4 raw would fire
        // 4 redundant initial reloads; drop() that synthetic first emission from each and add back
        // exactly one via onStart. conflate() then collapses a save's 4 near-simultaneous per-table
        // invalidations (all fired at commit) into a single reload instead of 4.
        val invalidations = merge(
            database.dayEntryQueries.selectByDate(dateString).asFlow().map { }.drop(1),
            database.metricQueries.selectByDate(dateString).asFlow().map { }.drop(1),
            database.mealQueries.selectByDate(dateString).asFlow().map { }.drop(1),
            database.achievementQueries.selectByDate(dateString).asFlow().map { }.drop(1),
        ).onStart { emit(Unit) }
        return invalidations
            .conflate()
            .map { withContext(dbDispatcher) { loadDayEntry(date) } }
            .distinctUntilChanged()
            .map { Result.success(it) }
            .catch { e ->
                if (e is CancellationException) throw e
                emit(Result.failure(e))
            }
    }

    override suspend fun saveDayEntry(dayEntry: DayEntry): Result<Unit> = runCatchingCancellable {
        require(dayEntry.metrics.map { it.title }.distinct().size == dayEntry.metrics.size) {
            "Duplicate metric titles are not allowed within a single day entry: " +
                dayEntry.metrics.map { it.title }
        }
        val dateString = dayEntry.date.toString()
        withContext(dbDispatcher) {
            database.dayEntryQueries.transaction {
                // Children are deleted before the parent row is replaced (not after) so that an
                // INSERT OR REPLACE on DayEntryEntity - which SQLite resolves via an internal
                // delete-then-insert - never has to delete a row still referenced by a FOREIGN KEY.
                database.metricQueries.deleteByDate(dateString)
                database.mealQueries.deleteByDate(dateString)
                database.achievementQueries.deleteByDate(dateString)

                database.dayEntryQueries.upsert(
                    date = dateString,
                    notes = dayEntry.notes,
                    isComplete = dayEntry.isComplete.toLong(),
                )

                dayEntry.metrics.forEach { metric ->
                    database.metricQueries.upsert(
                        date = dateString,
                        title = metric.title,
                        iconName = metric.iconName,
                        value_ = metric.value,
                        maxValue = metric.maxValue,
                        note = metric.note,
                    )
                }

                dayEntry.meals.forEach { meal ->
                    database.mealQueries.upsert(
                        id = meal.id,
                        date = dateString,
                        label = meal.label,
                        description = meal.mealDescription,
                        photoToken = meal.photoToken,
                        lovedIt = meal.lovedIt.toLong(),
                        sortOrder = meal.sortOrder,
                    )
                }

                dayEntry.achievements.forEach { achievement ->
                    database.achievementQueries.upsert(
                        id = achievement.id,
                        date = dateString,
                        text = achievement.text,
                        sortOrder = achievement.sortOrder,
                    )
                }
            }
        }
    }

    private fun loadDayEntry(date: LocalDate): DayEntry? {
        val dateString = date.toString()
        val entry = database.dayEntryQueries.selectByDate(dateString).executeAsOneOrNull() ?: return null
        return entry.toDomain(
            date = date,
            metrics = database.metricQueries.selectByDate(dateString).executeAsList(),
            meals = database.mealQueries.selectByDate(dateString).executeAsList(),
            achievements = database.achievementQueries.selectByDate(dateString).executeAsList(),
        )
    }
}

private fun DayEntryEntity.toDomain(
    date: LocalDate,
    metrics: List<MetricEntity>,
    meals: List<MealEntity>,
    achievements: List<AchievementEntity>,
): DayEntry = DayEntry(
    date = date,
    metrics = metrics.map { it.toDomain() },
    notes = notes,
    isComplete = isComplete.toBoolean(),
    meals = meals.map { it.toDomain() },
    achievements = achievements.map { it.toDomain() },
)

private fun MetricEntity.toDomain() = Metric(
    title = title,
    iconName = iconName,
    value = value_,
    maxValue = maxValue,
    note = note,
)

private fun MealEntity.toDomain() = Meal(
    id = id,
    label = label,
    mealDescription = description,
    photoToken = photoToken,
    lovedIt = lovedIt.toBoolean(),
    sortOrder = sortOrder,
)

private fun AchievementEntity.toDomain() = Achievement(
    id = id,
    text = text,
    sortOrder = sortOrder,
)

// SQLDelight has no zero-adapter Boolean support - DayEntryEntity.isComplete / MealEntity.lovedIt
// are stored as plain INTEGER (0/1) and converted at this mapping boundary instead.
private fun Boolean.toLong(): Long = if (this) 1L else 0L
private fun Long.toBoolean(): Boolean = this != 0L
