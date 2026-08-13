@file:OptIn(ExperimentalTime::class)

package dev.whole30journal.feature.daydetail.presentation.vm

import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.core.uistate.vm.StateFlowViewModel
import dev.whole30journal.core.utils.DateFormatter
import dev.whole30journal.core.utils.dateForDay
import dev.whole30journal.feature.program.domain.usecase.GetProgramUseCase
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class DayDetailViewModel(
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

    private var isLoaded = false

    override suspend fun applyUiAction(uiAction: DayDetailContract.UiAction) {
        when (uiAction) {
            is DayDetailContract.UiAction.OnAppear -> if (!isLoaded) loadDay(uiAction.dayNumber)
            DayDetailContract.UiAction.OnBackClick -> emitOutputEvent(DayDetailContract.OutputEvent.Close)
            DayDetailContract.UiAction.OnEditClick ->
                emitOutputEvent(DayDetailContract.OutputEvent.EditRequested(currentUiData.dayNumber))
        }
    }

    private suspend fun loadDay(dayNumber: Int) {
        val startDate = getProgram().getOrNull()?.startDate
        val dateLabel = startDate
            ?.let { dateFormatter(dateForDay(dayNumber, it), today(), DateFormatter.Style.Short) }
            .orEmpty()
        updateUiData(isLoading = false) { copy(dayNumber = dayNumber, dateLabel = dateLabel) }
        isLoaded = true
    }

    private fun today() = clock.todayIn(TimeZone.currentSystemDefault())
}
