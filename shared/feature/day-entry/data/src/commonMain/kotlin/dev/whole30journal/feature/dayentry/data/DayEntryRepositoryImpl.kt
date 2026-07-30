package dev.whole30journal.feature.dayentry.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.whole30journal.core.database.AchievementEntity
import dev.whole30journal.core.database.DayEntryEntity
import dev.whole30journal.core.database.MealEntity
import dev.whole30journal.core.database.MetricEntity
import dev.whole30journal.core.database.Whole30Database
import dev.whole30journal.feature.dayentry.domain.model.Achievement
import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import dev.whole30journal.feature.dayentry.domain.model.Meal
import dev.whole30journal.feature.dayentry.domain.model.Metric
import dev.whole30journal.feature.dayentry.domain.repository.DayEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

/** SQLDelight-backed [DayEntryRepository] - a day entry is stored as one row in `DayEntryEntity`
 * plus its child metric/meal/achievement rows, all keyed by [DayEntry.dayNumber]. */
internal class DayEntryRepositoryImpl(
    private val database: Whole30Database,
) : DayEntryRepository {

    override suspend fun getDayEntry(dayNumber: Long): Result<DayEntry?> = runCatching {
        withContext(Dispatchers.Default) { loadDayEntry(dayNumber) }
    }

    override fun observeDayEntry(dayNumber: Long): Flow<DayEntry?> = combine(
        database.dayEntryQueries.selectByDayNumber(dayNumber).asFlow().mapToOneOrNull(Dispatchers.Default),
        database.metricQueries.selectByDayNumber(dayNumber).asFlow().mapToList(Dispatchers.Default),
        database.mealQueries.selectByDayNumber(dayNumber).asFlow().mapToList(Dispatchers.Default),
        database.achievementQueries.selectByDayNumber(dayNumber).asFlow().mapToList(Dispatchers.Default),
    ) { entry, metrics, meals, achievements ->
        entry?.toDomain(metrics = metrics, meals = meals, achievements = achievements)
    }

    override suspend fun saveDayEntry(dayEntry: DayEntry): Result<Unit> = runCatching {
        withContext(Dispatchers.Default) {
            database.dayEntryQueries.transaction {
                database.dayEntryQueries.upsert(
                    dayNumber = dayEntry.dayNumber,
                    date = dayEntry.date,
                    notes = dayEntry.notes,
                    isComplete = dayEntry.isComplete.toLong(),
                )

                database.metricQueries.deleteByDayNumber(dayEntry.dayNumber)
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

                database.mealQueries.deleteByDayNumber(dayEntry.dayNumber)
                dayEntry.meals.forEach { meal ->
                    database.mealQueries.upsert(
                        id = meal.id,
                        dayNumber = dayEntry.dayNumber,
                        label = meal.label,
                        description = meal.description,
                        photoToken = meal.photoToken,
                        lovedIt = meal.lovedIt.toLong(),
                        sortOrder = meal.sortOrder,
                    )
                }

                database.achievementQueries.deleteByDayNumber(dayEntry.dayNumber)
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
    dayNumber = dayNumber,
    label = label,
    description = description,
    photoToken = photoToken,
    lovedIt = lovedIt.toBoolean(),
    sortOrder = sortOrder,
)

private fun AchievementEntity.toDomain() = Achievement(
    id = id,
    dayNumber = dayNumber,
    text = text,
    sortOrder = sortOrder,
)

// SQLDelight has no zero-adapter Boolean support - DayEntryEntity.isComplete / MealEntity.lovedIt
// are stored as plain INTEGER (0/1) and converted at this mapping boundary instead.
private fun Boolean.toLong(): Long = if (this) 1L else 0L
private fun Long.toBoolean(): Boolean = this != 0L
