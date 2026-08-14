@file:OptIn(ExperimentalTime::class)

package dev.whole30journal.feature.daydetail.presentation.vm

import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.core.uistate.vm.StateFlowViewModel
import dev.whole30journal.core.utils.DateFormatter
import dev.whole30journal.core.utils.dateForDay
import dev.whole30journal.feature.daydetail.presentation.generated.resources.Res
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_metric_cravings_title
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_metric_energy_title
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_metric_mood_title
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_metric_sleep_title
import dev.whole30journal.feature.dayentry.domain.model.DayEntry
import dev.whole30journal.feature.dayentry.domain.model.Meal
import dev.whole30journal.feature.dayentry.domain.model.MetricTitle
import dev.whole30journal.feature.dayentry.domain.usecase.ObserveDayEntryUseCase
import dev.whole30journal.feature.program.domain.usecase.GetProgramUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class DayDetailViewModel(
    private val observeDayEntry: ObserveDayEntryUseCase,
    private val getProgram: GetProgramUseCase,
    private val dateFormatter: DateFormatter,
    private val clock: Clock = Clock.System,
) : StateFlowViewModel<
    DayDetailContract.UiData,
    DayDetailContract.UiAction,
    DayDetailContract.UiEvent,
    DayDetailContract.OutputEvent,
    >(
    initialState = UiStateAware.UiState(isLoading = true, uiData = DayDetailContract.UiData()),
) {

    private var isObserving = false
    private var requestedDayNumber: Int? = null

    override suspend fun applyUiAction(uiAction: DayDetailContract.UiAction) {
        when (uiAction) {
            is DayDetailContract.UiAction.OnAppear -> {
                requestedDayNumber = uiAction.dayNumber
                if (!isObserving) observeDay(uiAction.dayNumber)
            }
            DayDetailContract.UiAction.OnBackClick -> emitOutputEvent(DayDetailContract.OutputEvent.Close)
            DayDetailContract.UiAction.OnEditClick ->
                requestedDayNumber?.let { emitOutputEvent(DayDetailContract.OutputEvent.EditRequested(it)) }
        }
    }

    private suspend fun observeDay(dayNumber: Int) {
        isObserving = true
        val startDate = getProgram().getOrNull()?.startDate
        val dateLabel = startDate
            ?.let { dateFormatter(dateForDay(dayNumber, it), today(), DateFormatter.Style.Short) }
            .orEmpty()
        val metricTitles = metricTitleLabels()

        observeDayEntry(dayNumber.toLong()).collectLatest { result ->
            val entry = result.getOrNull()
            updateUiData(isLoading = false) {
                copy(
                    dayNumber = dayNumber,
                    dateLabel = dateLabel,
                    // A program's day rows are pre-seeded (see ProgramRepositoryImpl.configureProgram)
                    // so `entry` is non-null for every in-range day even before it's ever been logged -
                    // only saved metrics distinguish a real entry from that bare seed row.
                    hasEntry = entry?.metrics?.isNotEmpty() == true,
                    isComplete = entry?.isComplete ?: false,
                    overallScore = entry?.scoreFor(MetricTitle.OVERALL),
                    metrics = entry?.let { metricSummaries(it, metricTitles) }.orEmpty(),
                    meals = entry?.meals.orEmpty()
                        .filter { it.mealDescription.isNotBlank() || it.photoToken != null || it.lovedIt }
                        .map { it.toMealSummary() },
                    achievements = entry?.achievements.orEmpty().map { it.text }.filter { it.isNotBlank() },
                    notes = entry?.notes.orEmpty(),
                )
            }
        }
    }

    private suspend fun metricTitleLabels(): Map<String, String> = mapOf(
        MetricTitle.ENERGY to getString(Res.string.day_detail_metric_energy_title),
        MetricTitle.MOOD to getString(Res.string.day_detail_metric_mood_title),
        MetricTitle.SLEEP to getString(Res.string.day_detail_metric_sleep_title),
        MetricTitle.CRAVINGS to getString(Res.string.day_detail_metric_cravings_title),
    )

    private fun metricSummaries(entry: DayEntry, titleLabels: Map<String, String>): List<DayDetailContract.MetricSummary> =
        titleLabels.mapNotNull { (key, label) ->
            val metric = entry.metrics.firstOrNull { it.title == key } ?: return@mapNotNull null
            DayDetailContract.MetricSummary(title = label, note = metric.note, score = metric.value?.toInt())
        }

    private fun today() = clock.todayIn(TimeZone.currentSystemDefault())
}

private fun DayEntry.scoreFor(title: String): Int? = metrics.firstOrNull { it.title == title }?.value?.toInt()

private fun Meal.toMealSummary() = DayDetailContract.MealSummary(
    id = id,
    label = label,
    description = mealDescription,
    photoToken = photoToken,
    lovedIt = lovedIt,
)
