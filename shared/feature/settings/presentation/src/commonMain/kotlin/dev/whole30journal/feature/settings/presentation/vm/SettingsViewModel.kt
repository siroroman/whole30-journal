@file:OptIn(ExperimentalTime::class)

package dev.whole30journal.feature.settings.presentation.vm

import androidx.lifecycle.viewModelScope
import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.core.uistate.vm.StateFlowViewModel
import dev.whole30journal.core.utils.DateFormatter
import dev.whole30journal.core.utils.dateForDay
import dev.whole30journal.feature.program.domain.usecase.ConfigureProgramUseCase
import dev.whole30journal.feature.program.domain.usecase.GetProgramUseCase
import dev.whole30journal.feature.settings.presentation.generated.resources.Res
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_save_error
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SettingsViewModel(
    private val getProgram: GetProgramUseCase,
    private val configureProgram: ConfigureProgramUseCase,
    private val dateFormatter: DateFormatter,
    private val clock: Clock = Clock.System,
) : StateFlowViewModel<
    SettingsContract.UiData,
    SettingsContract.UiAction,
    SettingsContract.UiEvent,
    SettingsContract.OutputEvent,
    >(
    initialState = UiStateAware.UiState(isLoading = true, uiData = SettingsContract.UiData()),
) {

    init {
        viewModelScope.launch { load() }
    }

    override suspend fun applyUiAction(uiAction: SettingsContract.UiAction) {
        when (uiAction) {
            SettingsContract.UiAction.OnChangeStartDateClick -> updateUiData { copy(isDatePickerVisible = true) }
            is SettingsContract.UiAction.OnStartDateSelected -> selectStartDate(uiAction.date)
            SettingsContract.UiAction.OnDatePickerDismiss -> updateUiData { copy(isDatePickerVisible = false) }
            is SettingsContract.UiAction.OnDurationSelected -> selectDuration(uiAction.durationDays)
            SettingsContract.UiAction.OnConfirmClick -> save()
            SettingsContract.UiAction.OnCancelClick -> emitOutputEvent(SettingsContract.OutputEvent.Cancelled)
        }
    }

    private suspend fun load() {
        val program = getProgram().getOrNull()
        val startDate = program?.startDate ?: today()
        val durationDays = program?.durationDays?.toInt() ?: DEFAULT_DURATION_DAYS
        val mode = if (program != null) SettingsContract.Mode.Edit else SettingsContract.Mode.Setup
        updateUiData(isLoading = false) { copy(mode = mode, startDate = startDate, durationDays = durationDays) }
        refreshLabels(startDate, durationDays)
    }

    private suspend fun selectStartDate(date: LocalDate) {
        updateUiData { copy(startDate = date, isDatePickerVisible = false) }
        refreshLabels(date, currentUiData.durationDays)
    }

    private suspend fun selectDuration(durationDays: Int) {
        updateUiData { copy(durationDays = durationDays) }
        val startDate = currentUiData.startDate ?: return
        refreshLabels(startDate, durationDays)
    }

    private suspend fun save() {
        val data = currentUiData
        val startDate = data.startDate ?: return
        updateUiData { copy(isSaving = true) }
        configureProgram(startDate = startDate, durationDays = data.durationDays.toLong()).fold(
            onSuccess = { emitOutputEvent(SettingsContract.OutputEvent.Saved) },
            onFailure = {
                val message = getString(Res.string.settings_save_error)
                updateUiData { copy(isSaving = false) }
                updateUiEvents { it + SettingsContract.UiEvent.ShowSaveError(message) }
            },
        )
    }

    private suspend fun refreshLabels(startDate: LocalDate, durationDays: Int) {
        val today = today()
        val startLabel = dateFormatter(startDate, today, DateFormatter.Style.Long)
        val endLabel = dateFormatter(dateForDay(durationDays, startDate), today, DateFormatter.Style.Short)
        updateUiData { copy(startDateLabel = startLabel, endDateLabel = endLabel) }
    }

    private fun today(): LocalDate = clock.todayIn(TimeZone.currentSystemDefault())
}

private const val DEFAULT_DURATION_DAYS = 30
