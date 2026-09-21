@file:OptIn(ExperimentalTime::class)

package dev.whole30journal.feature.dayentry.presentation.vm

import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.core.uistate.vm.StateFlowViewModel
import dev.whole30journal.core.utils.DateFormatter
import dev.whole30journal.core.utils.dateForDay
import dev.whole30journal.feature.dayentry.domain.model.Achievement
import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import dev.whole30journal.feature.dayentry.domain.model.Meal
import dev.whole30journal.feature.dayentry.domain.model.Metric
import dev.whole30journal.feature.dayentry.domain.model.MetricTitle
import dev.whole30journal.feature.dayentry.domain.model.overallScore
import dev.whole30journal.feature.dayentry.domain.usecase.GetDayEntryUseCase
import dev.whole30journal.feature.dayentry.domain.usecase.SaveDayEntryUseCase
import dev.whole30journal.feature.dayentry.presentation.generated.resources.Res
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meal_label_numbered
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_save_error
import dev.whole30journal.feature.program.domain.usecase.GetProgramUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class DayEntryViewModel(
    private val getDayEntry: GetDayEntryUseCase,
    private val saveDayEntry: SaveDayEntryUseCase,
    private val getProgram: GetProgramUseCase,
    private val dateFormatter: DateFormatter,
    private val clock: Clock = Clock.System,
) : StateFlowViewModel<
    DayEntryContract.UiData,
    DayEntryContract.UiAction,
    DayEntryContract.UiEvent,
    DayEntryContract.OutputEvent,
    >(
    initialState = UiStateAware.UiState(isLoading = true, uiData = DayEntryContract.UiData()),
) {

    private var isLoaded = false

    override suspend fun applyUiAction(uiAction: DayEntryContract.UiAction) {
        when (uiAction) {
            is DayEntryContract.UiAction.OnAppear ->
                if (!isLoaded) loadDayEntry(uiAction.dayNumber)
            is DayEntryContract.UiAction.OnScoreChange ->
                updateUiData { withScore(uiAction.metric, uiAction.score) }
            is DayEntryContract.UiAction.OnNoteChange ->
                updateUiData { withNote(uiAction.metric, uiAction.note) }
            is DayEntryContract.UiAction.OnAchievementTextChange ->
                updateUiData { withAchievementText(uiAction.id, uiAction.text) }
            DayEntryContract.UiAction.OnAddAchievementClick -> addAchievement()
            DayEntryContract.UiAction.OnAddMealClick -> addMeal()
            is DayEntryContract.UiAction.OnMealDescriptionChange ->
                updateUiData { withMealDescription(uiAction.id, uiAction.description) }
            is DayEntryContract.UiAction.OnMealLovedToggle ->
                updateUiData { withMealLovedToggled(uiAction.id) }
            is DayEntryContract.UiAction.OnAddMealPhotoClick ->
                updateUiData { copy(pendingPhotoMealId = uiAction.id) }
            is DayEntryContract.UiAction.OnMealPhotoPick ->
                updateUiData { withMealPhoto(uiAction.mealId, uiAction.token) }
            is DayEntryContract.UiAction.OnMealPhotoRemove ->
                updateUiData { withMealPhotoRemoved(uiAction.mealId) }
            DayEntryContract.UiAction.OnPhotoSourceDismiss ->
                updateUiData { copy(pendingPhotoMealId = null) }
            is DayEntryContract.UiAction.OnDeleteMealClick ->
                updateUiData { copy(pendingDeleteMealId = uiAction.id) }
            DayEntryContract.UiAction.OnDeleteMealConfirm -> confirmDeleteMeal()
            DayEntryContract.UiAction.OnDeleteMealDismiss ->
                updateUiData { copy(pendingDeleteMealId = null) }
            is DayEntryContract.UiAction.OnMealReorder ->
                updateUiData { copy(meals = meals.moved(uiAction.fromIndex, uiAction.toIndex)) }
            is DayEntryContract.UiAction.OnDeleteAchievementClick ->
                updateUiData { copy(pendingDeleteAchievementId = uiAction.id) }
            DayEntryContract.UiAction.OnDeleteAchievementConfirm -> confirmDeleteAchievement()
            DayEntryContract.UiAction.OnDeleteAchievementDismiss ->
                updateUiData { copy(pendingDeleteAchievementId = null) }
            is DayEntryContract.UiAction.OnAchievementReorder ->
                updateUiData { copy(achievements = achievements.moved(uiAction.fromIndex, uiAction.toIndex)) }
            is DayEntryContract.UiAction.OnNotesChange ->
                updateUiData { copy(notes = uiAction.notes) }
            DayEntryContract.UiAction.OnCompleteToggle ->
                updateUiData { copy(isComplete = !isComplete) }
            DayEntryContract.UiAction.OnSaveClick -> save()
            DayEntryContract.UiAction.OnCancelClick -> emitOutputEvent(DayEntryContract.OutputEvent.Close)
        }
    }

    private suspend fun loadDayEntry(dayNumber: Int) {
        updateIsLoading(true)
        val (program, entry) = coroutineScope {
            val programDeferred = async { getProgram().getOrNull() }
            val entryDeferred = async { getDayEntry(dayNumber.toLong()).getOrNull() }
            programDeferred.await() to entryDeferred.await()
        }
        val startDate = program?.startDate
        val dateLabel = startDate?.let { dateFormatter(dateForDay(dayNumber, it), today(), DateFormatter.Style.Short) }.orEmpty()
        val totalDays = program?.durationDays?.toInt() ?: DEFAULT_TOTAL_DAYS

        val draft = entry?.takeIf { it.isLogged }?.toUiData(dayNumber, dateLabel, totalDays, startDate)
            ?: defaultUiData(dayNumber, dateLabel, totalDays, startDate)
        updateUiData(isLoading = false) { draft }
        isLoaded = true
    }

    private suspend fun save() {
        val data = currentUiData
        updateUiData { copy(isSaving = true) }

        val entry = DayEntry(
            dayNumber = data.dayNumber.toLong(),
            date = data.programStartDate?.let { dateForDay(data.dayNumber, it).toString() }.orEmpty(),
            metrics = listOf(
                Metric(MetricTitle.ENERGY, "energy", data.energy.score?.toLong(), MAX_SCORE, data.energy.note),
                Metric(MetricTitle.MOOD, "mood", data.mood.score?.toLong(), MAX_SCORE, data.mood.note),
                Metric(MetricTitle.SLEEP, "sleep", data.sleep.score?.toLong(), MAX_SCORE, data.sleep.note),
                Metric(MetricTitle.CRAVINGS, "cravings", data.cravings.score?.toLong(), MAX_SCORE, data.cravings.note),
                Metric(MetricTitle.OVERALL, "leaf", data.overallScore?.toLong(), MAX_SCORE, ""),
            ),
            notes = data.notes,
            isComplete = data.isComplete,
            meals = data.meals.mapIndexed { index, meal ->
                Meal(
                    id = meal.id,
                    label = getString(Res.string.day_entry_meal_label_numbered, index + 1),
                    mealDescription = meal.description,
                    photoToken = meal.photoToken,
                    lovedIt = meal.lovedIt,
                    sortOrder = index.toLong(),
                )
            },
            achievements = data.achievements
                .filter { it.text.isNotBlank() }
                .mapIndexed { index, achievement -> Achievement(id = achievement.id, text = achievement.text, sortOrder = index.toLong()) },
        )

        saveDayEntry(entry).fold(
            onSuccess = { emitOutputEvent(DayEntryContract.OutputEvent.Close) },
            onFailure = {
                val message = getString(Res.string.day_entry_save_error)
                updateUiData { copy(isSaving = false) }
                updateUiEvents { it + DayEntryContract.UiEvent.ShowSaveError(message) }
            },
        )
    }

    private fun addMeal() {
        updateUiData {
            copy(meals = meals + DayEntryContract.MealEntry(id = "day-$dayNumber-meal-added-${meals.size}"))
        }
    }

    private fun addAchievement() {
        updateUiData {
            copy(
                achievements = achievements +
                    DayEntryContract.AchievementEntry(id = "day-$dayNumber-achievement-added-${achievements.size}", text = ""),
            )
        }
    }

    private fun confirmDeleteMeal() {
        val id = currentUiData.pendingDeleteMealId ?: return
        updateUiData { copy(meals = meals.filterNot { it.id == id }, pendingDeleteMealId = null) }
    }

    private fun confirmDeleteAchievement() {
        val id = currentUiData.pendingDeleteAchievementId ?: return
        updateUiData { copy(achievements = achievements.filterNot { it.id == id }, pendingDeleteAchievementId = null) }
    }

    private fun defaultUiData(
        dayNumber: Int,
        dateLabel: String,
        totalDays: Int,
        startDate: LocalDate?,
    ): DayEntryContract.UiData = DayEntryContract.UiData(
        dayNumber = dayNumber,
        dateLabel = dateLabel,
        totalDays = totalDays,
        programStartDate = startDate,
        meals = defaultMeals(dayNumber),
    )

    private fun defaultMeals(dayNumber: Int): List<DayEntryContract.MealEntry> = listOf(
        DayEntryContract.MealEntry(id = "day-$dayNumber-meal-slot-1"),
        DayEntryContract.MealEntry(id = "day-$dayNumber-meal-slot-2"),
        DayEntryContract.MealEntry(id = "day-$dayNumber-meal-slot-3"),
    )

    private fun today(): LocalDate = clock.todayIn(TimeZone.currentSystemDefault())

    private fun DayEntry.toUiData(
        dayNumber: Int,
        dateLabel: String,
        totalDays: Int,
        startDate: LocalDate?,
    ): DayEntryContract.UiData {
        fun metricEntry(title: String) = metrics.firstOrNull { it.title == title }
            ?.let { DayEntryContract.MetricEntry(score = it.value?.toInt(), note = it.note) }
            ?: DayEntryContract.MetricEntry()

        val energy = metricEntry(MetricTitle.ENERGY)
        val mood = metricEntry(MetricTitle.MOOD)
        val sleep = metricEntry(MetricTitle.SLEEP)
        val cravings = metricEntry(MetricTitle.CRAVINGS)

        return DayEntryContract.UiData(
            dayNumber = dayNumber,
            dateLabel = dateLabel,
            totalDays = totalDays,
            programStartDate = startDate,
            energy = energy,
            mood = mood,
            sleep = sleep,
            cravings = cravings,
            overallScore = computeOverall(energy, mood, sleep, cravings),
            achievements = achievements
                .sortedBy { it.sortOrder }
                .map { DayEntryContract.AchievementEntry(id = it.id, text = it.text) },
            meals = meals.takeIf { it.isNotEmpty() }
                ?.sortedBy { it.sortOrder }
                ?.map {
                    DayEntryContract.MealEntry(
                        id = it.id,
                        description = it.mealDescription,
                        photoToken = it.photoToken,
                        lovedIt = it.lovedIt,
                    )
                }
                ?: defaultMeals(dayNumber),
            notes = notes,
            isComplete = isComplete,
        )
    }
}

private const val DEFAULT_TOTAL_DAYS = 30
private const val MAX_SCORE = 10L

private fun computeOverall(vararg entries: DayEntryContract.MetricEntry): Int? =
    overallScore(entries.map { it.score })

private fun DayEntryContract.UiData.withScore(metric: DayEntryContract.MetricKind, score: Int): DayEntryContract.UiData {
    val updated = when (metric) {
        DayEntryContract.MetricKind.Energy -> copy(energy = energy.copy(score = score))
        DayEntryContract.MetricKind.Mood -> copy(mood = mood.copy(score = score))
        DayEntryContract.MetricKind.Sleep -> copy(sleep = sleep.copy(score = score))
        DayEntryContract.MetricKind.Cravings -> copy(cravings = cravings.copy(score = score))
    }
    return updated.copy(overallScore = computeOverall(updated.energy, updated.mood, updated.sleep, updated.cravings))
}

private fun DayEntryContract.UiData.withNote(metric: DayEntryContract.MetricKind, note: String): DayEntryContract.UiData = when (metric) {
    DayEntryContract.MetricKind.Energy -> copy(energy = energy.copy(note = note))
    DayEntryContract.MetricKind.Mood -> copy(mood = mood.copy(note = note))
    DayEntryContract.MetricKind.Sleep -> copy(sleep = sleep.copy(note = note))
    DayEntryContract.MetricKind.Cravings -> copy(cravings = cravings.copy(note = note))
}

private fun DayEntryContract.UiData.withAchievementText(id: String, text: String): DayEntryContract.UiData =
    copy(achievements = achievements.map { if (it.id == id) it.copy(text = text) else it })

private fun DayEntryContract.UiData.withMealDescription(id: String, description: String): DayEntryContract.UiData =
    copy(meals = meals.map { if (it.id == id) it.copy(description = description) else it })

private fun DayEntryContract.UiData.withMealLovedToggled(id: String): DayEntryContract.UiData =
    copy(meals = meals.map { if (it.id == id) it.copy(lovedIt = !it.lovedIt) else it })

private fun DayEntryContract.UiData.withMealPhoto(id: String, token: String): DayEntryContract.UiData =
    copy(meals = meals.map { if (it.id == id) it.copy(photoToken = token) else it }, pendingPhotoMealId = null)

private fun DayEntryContract.UiData.withMealPhotoRemoved(id: String): DayEntryContract.UiData =
    copy(meals = meals.map { if (it.id == id) it.copy(photoToken = null) else it }, pendingPhotoMealId = null)

private fun <T> List<T>.moved(fromIndex: Int, toIndex: Int): List<T> =
    if (fromIndex == toIndex || fromIndex !in indices || toIndex !in indices) {
        this
    } else {
        toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }
