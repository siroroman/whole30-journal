package dev.whole30journal.feature.dayentry.presentation.vm

import androidx.compose.runtime.Immutable
import dev.whole30journal.core.uistate.UiActionAware
import dev.whole30journal.core.uistate.UiStateAware
import kotlinx.datetime.LocalDate

object DayEntryContract {

    enum class MetricKind { Energy, Mood, Sleep, Cravings }

    @Immutable
    data class MetricEntry(val score: Int? = null, val note: String = "")

    @Immutable
    data class AchievementEntry(val id: String, val text: String)

    @Immutable
    data class MealEntry(
        val id: String,
        val label: String,
        val description: String = "",
        val photoToken: String? = null,
        val lovedIt: Boolean = false,
    )

    @Immutable
    data class UiData(
        val dayNumber: Int = 0,
        val dateLabel: String = "",
        val totalDays: Int = 30,
        val programStartDate: LocalDate? = null,
        val energy: MetricEntry = MetricEntry(),
        val mood: MetricEntry = MetricEntry(),
        val sleep: MetricEntry = MetricEntry(),
        val cravings: MetricEntry = MetricEntry(),
        val overallScore: Int? = null,
        val achievements: List<AchievementEntry> = emptyList(),
        val meals: List<MealEntry> = emptyList(),
        val pendingPhotoMealId: String? = null,
        val notes: String = "",
        val isComplete: Boolean = true,
        val isSaving: Boolean = false,
    ) : UiStateAware.UiData

    sealed interface UiAction : UiActionAware.UiAction {
        data class OnAppear(val dayNumber: Int) : UiAction
        data class OnScoreChange(val metric: MetricKind, val score: Int) : UiAction
        data class OnNoteChange(val metric: MetricKind, val note: String) : UiAction
        data class OnAchievementTextChange(val id: String, val text: String) : UiAction
        data object OnAddAchievementClick : UiAction
        data object OnAddMealClick : UiAction
        data class OnMealDescriptionChange(val id: String, val description: String) : UiAction
        data class OnMealLovedToggle(val id: String) : UiAction
        data class OnAddMealPhotoClick(val id: String) : UiAction
        data class OnMealPhotoPick(val mealId: String, val token: String) : UiAction
        data object OnPhotoSourceDismiss : UiAction
        data class OnNotesChange(val notes: String) : UiAction
        data object OnCompleteToggle : UiAction
        data object OnSaveClick : UiAction
        data object OnCancelClick : UiAction
    }

    sealed interface UiEvent : UiStateAware.UiEvent {
        data class ShowSaveError(val message: String) : UiEvent
    }

    sealed interface OutputEvent : UiStateAware.OutputEvent {
        data object Close : OutputEvent
    }
}
