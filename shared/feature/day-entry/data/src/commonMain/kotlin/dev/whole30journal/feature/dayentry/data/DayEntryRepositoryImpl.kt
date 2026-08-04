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
import dev.whole30journal.core.database.databaseDispatcher as dbDispatcher

/** SQLDelight-backed [DayEntryRepository] - a day entry is stored as one row in `DayEntryEntity`
 * plus its child metric/meal/achievement rows, all keyed by [DayEntry.dayNumber]. */
internal class DayEntryRepositoryImpl(
    private val database: Whole30Database,
) : DayEntryRepository {

    override suspend fun getDayEntry(dayNumber: Long): Result<DayEntry?> = runCatchingCancellable {
        withContext(dbDispatcher) { loadDayEntry(dayNumber) }
    }

    // A save touches all 4 tables in one transaction; combine()-ing 4 independently-invalidated
    // per-table flows would let a collector observe a torn intermediate state (e.g. the day row
    // updated but children flows still on their last-cached value). Merging into a single "something
    // changed" signal and re-reading everything fresh on each tick keeps every emission consistent.
    override fun observeDayEntry(dayNumber: Long): Flow<Result<DayEntry?>> {
        // Each asFlow() source emits once immediately on subscribe, so merging all 4 raw would fire
        // 4 redundant initial reloads; drop() that synthetic first emission from each and add back
        // exactly one via onStart. conflate() then collapses a save's 4 near-simultaneous per-table
        // invalidations (all fired at commit) into a single reload instead of 4.
        val invalidations = merge(
            database.dayEntryQueries.selectByDayNumber(dayNumber).asFlow().map { }.drop(1),
            database.metricQueries.selectByDayNumber(dayNumber).asFlow().map { }.drop(1),
            database.mealQueries.selectByDayNumber(dayNumber).asFlow().map { }.drop(1),
            database.achievementQueries.selectByDayNumber(dayNumber).asFlow().map { }.drop(1),
        ).onStart { emit(Unit) }
        return invalidations
            .conflate()
            .map { withContext(dbDispatcher) { loadDayEntry(dayNumber) } }
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
        withContext(dbDispatcher) {
            database.dayEntryQueries.transaction {
                // Children are deleted before the parent row is replaced (not after) so that an
                // INSERT OR REPLACE on DayEntryEntity - which SQLite resolves via an internal
                // delete-then-insert - never has to delete a row still referenced by a FOREIGN KEY.
                database.metricQueries.deleteByDayNumber(dayEntry.dayNumber)
                database.mealQueries.deleteByDayNumber(dayEntry.dayNumber)
                database.achievementQueries.deleteByDayNumber(dayEntry.dayNumber)

                database.dayEntryQueries.upsert(
                    dayNumber = dayEntry.dayNumber,
                    date = dayEntry.date,
                    notes = dayEntry.notes,
                    isComplete = dayEntry.isComplete.toLong(),
                )

                dayEntry.metrics.forEach { metric ->
                    database.metricQueries.upsert(
                        dayNumber = dayEntry.dayNumber,
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
                        dayNumber = dayEntry.dayNumber,
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
                        dayNumber = dayEntry.dayNumber,
                        text = achievement.text,
                        sortOrder = achievement.sortOrder,
                    )
                }
            }
        }
    }

    private fun loadDayEntry(dayNumber: Long): DayEntry? {
        val entry = database.dayEntryQueries.selectByDayNumber(dayNumber).executeAsOneOrNull() ?: return null
        return entry.toDomain(
            metrics = database.metricQueries.selectByDayNumber(dayNumber).executeAsList(),
            meals = database.mealQueries.selectByDayNumber(dayNumber).executeAsList(),
            achievements = database.achievementQueries.selectByDayNumber(dayNumber).executeAsList(),
        )
    }
}

private fun DayEntryEntity.toDomain(
    metrics: List<MetricEntity>,
    meals: List<MealEntity>,
    achievements: List<AchievementEntity>,
): DayEntry = DayEntry(
    dayNumber = dayNumber,
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
