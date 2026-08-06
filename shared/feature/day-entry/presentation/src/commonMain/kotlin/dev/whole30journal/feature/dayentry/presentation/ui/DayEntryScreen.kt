package dev.whole30journal.feature.dayentry.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import dev.whole30journal.core.designsystem.components.DSTextField
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.feature.dayentry.presentation.generated.resources.Res
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_cancel_button
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_day_title
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_metric_cravings_high
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_metric_cravings_low
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_metric_cravings_title
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_metric_energy_high
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_metric_energy_low
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_metric_energy_title
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_metric_mood_high
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_metric_mood_low
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_metric_mood_title
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_metric_sleep_high
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_metric_sleep_low
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_metric_sleep_title
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_note_placeholder
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_notes_placeholder
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_notes_title
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_save_button_short
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_section_how_i_felt
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun DayEntryScreen(
    state: UiStateAware.UiState<DayEntryContract.UiData, DayEntryContract.UiEvent>,
    onUiAction: (DayEntryContract.UiAction) -> Unit,
    onUiEventConsume: (DayEntryContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    DSTheme {
        Scaffold(
            modifier = modifier,
            containerColor = DSTheme.colors.bg,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                DayEntryTopBar(
                    dayNumber = state.uiData.dayNumber,
                    dateLabel = state.uiData.dateLabel,
                    onCancelClick = { onUiAction(DayEntryContract.UiAction.OnCancelClick) },
                    onSaveClick = { onUiAction(DayEntryContract.UiAction.OnSaveClick) },
                )
            },
            bottomBar = {
                DayEntryFooter(
                    dayNumber = state.uiData.dayNumber,
                    isComplete = state.uiData.isComplete,
                    isSaving = state.uiData.isSaving,
                    onCompleteToggle = { onUiAction(DayEntryContract.UiAction.OnCompleteToggle) },
                    onSaveClick = { onUiAction(DayEntryContract.UiAction.OnSaveClick) },
                )
            },
        ) { contentPadding ->
            DayEntryContent(
                uiData = state.uiData,
                onUiAction = onUiAction,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize(),
            )
            HandleUiEvents(events = state.uiEvents, snackbarHostState = snackbarHostState, onConsume = onUiEventConsume)
        }
    }
}

@Composable
private fun HandleUiEvents(
    events: List<DayEntryContract.UiEvent>,
    snackbarHostState: SnackbarHostState,
    onConsume: (DayEntryContract.UiEvent) -> Unit,
) {
    events.forEach { event ->
        when (event) {
            is DayEntryContract.UiEvent.ShowSaveError -> {
                LaunchedEffect(event) {
                    snackbarHostState.showSnackbar(event.message)
                    onConsume(event)
                }
            }
        }
    }
}

@Composable
private fun DayEntryTopBar(
    dayNumber: Int,
    dateLabel: String,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth().statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = DSSpacing.space9, vertical = DSSpacing.space6),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.day_entry_cancel_button),
                style = DSTheme.typography.textLg,
                color = colors.textSecondary,
                modifier = Modifier.clickable(onClick = onCancelClick),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(Res.string.day_entry_day_title, dayNumber), style = DSTheme.typography.textMd, color = colors.text)
                Text(text = dateLabel, style = DSTheme.typography.textXs, color = colors.textTertiary)
            }
            Text(
                text = stringResource(Res.string.day_entry_save_button_short),
                style = DSTheme.typography.textLg.copy(fontWeight = FontWeight.Bold),
                color = colors.accent,
                modifier = Modifier.clickable(onClick = onSaveClick),
            )
        }
        HorizontalDivider(color = colors.divider)
    }
}

@Composable
private fun DayEntryContent(
    uiData: DayEntryContract.UiData,
    onUiAction: (DayEntryContract.UiAction) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DSSpacing.space7, vertical = DSSpacing.space7),
        verticalArrangement = Arrangement.spacedBy(DSSpacing.space8),
    ) {
        Text(text = stringResource(Res.string.day_entry_section_how_i_felt), style = DSTheme.typography.textLg, color = DSTheme.colors.text)

        MetricScoreCard(
            title = stringResource(Res.string.day_entry_metric_energy_title),
            lowLabel = stringResource(Res.string.day_entry_metric_energy_low),
            highLabel = stringResource(Res.string.day_entry_metric_energy_high),
            score = uiData.energy.score,
            note = uiData.energy.note,
            notePlaceholder = stringResource(Res.string.day_entry_note_placeholder),
            onScoreChange = { onUiAction(DayEntryContract.UiAction.OnScoreChange(DayEntryContract.MetricKind.Energy, it)) },
            onNoteChange = { onUiAction(DayEntryContract.UiAction.OnNoteChange(DayEntryContract.MetricKind.Energy, it)) },
        )
        MetricScoreCard(
            title = stringResource(Res.string.day_entry_metric_mood_title),
            lowLabel = stringResource(Res.string.day_entry_metric_mood_low),
            highLabel = stringResource(Res.string.day_entry_metric_mood_high),
            score = uiData.mood.score,
            note = uiData.mood.note,
            notePlaceholder = stringResource(Res.string.day_entry_note_placeholder),
            onScoreChange = { onUiAction(DayEntryContract.UiAction.OnScoreChange(DayEntryContract.MetricKind.Mood, it)) },
            onNoteChange = { onUiAction(DayEntryContract.UiAction.OnNoteChange(DayEntryContract.MetricKind.Mood, it)) },
        )
        MetricScoreCard(
            title = stringResource(Res.string.day_entry_metric_sleep_title),
            lowLabel = stringResource(Res.string.day_entry_metric_sleep_low),
            highLabel = stringResource(Res.string.day_entry_metric_sleep_high),
            score = uiData.sleep.score,
            note = uiData.sleep.note,
            notePlaceholder = stringResource(Res.string.day_entry_note_placeholder),
            onScoreChange = { onUiAction(DayEntryContract.UiAction.OnScoreChange(DayEntryContract.MetricKind.Sleep, it)) },
            onNoteChange = { onUiAction(DayEntryContract.UiAction.OnNoteChange(DayEntryContract.MetricKind.Sleep, it)) },
        )
        MetricScoreCard(
            title = stringResource(Res.string.day_entry_metric_cravings_title),
            lowLabel = stringResource(Res.string.day_entry_metric_cravings_low),
            highLabel = stringResource(Res.string.day_entry_metric_cravings_high),
            score = uiData.cravings.score,
            note = uiData.cravings.note,
            notePlaceholder = stringResource(Res.string.day_entry_note_placeholder),
            onScoreChange = { onUiAction(DayEntryContract.UiAction.OnScoreChange(DayEntryContract.MetricKind.Cravings, it)) },
            onNoteChange = { onUiAction(DayEntryContract.UiAction.OnNoteChange(DayEntryContract.MetricKind.Cravings, it)) },
        )
        OverallScoreCard(score = uiData.overallScore)

        AchievementsSection(
            achievements = uiData.achievements,
            onTextChange = { id, text -> onUiAction(DayEntryContract.UiAction.OnAchievementTextChange(id, text)) },
            onAddClick = { onUiAction(DayEntryContract.UiAction.OnAddAchievementClick) },
        )

        MealsSection(
            meals = uiData.meals,
            onDescriptionChange = { id, description -> onUiAction(DayEntryContract.UiAction.OnMealDescriptionChange(id, description)) },
            onLovedToggle = { onUiAction(DayEntryContract.UiAction.OnMealLovedToggle(it)) },
            onAddPhotoClick = { onUiAction(DayEntryContract.UiAction.OnAddMealPhotoClick(it)) },
            onAddMealClick = { onUiAction(DayEntryContract.UiAction.OnAddMealClick) },
        )

        Column(verticalArrangement = Arrangement.spacedBy(DSSpacing.space3)) {
            Text(text = stringResource(Res.string.day_entry_notes_title), style = DSTheme.typography.textLg, color = DSTheme.colors.text)
            DSTextField(
                value = uiData.notes,
                onValueChange = { onUiAction(DayEntryContract.UiAction.OnNotesChange(it)) },
                placeholder = stringResource(Res.string.day_entry_notes_placeholder),
                singleLine = false,
                minLines = 4,
                containerColor = DSTheme.colors.surface,
            )
        }
    }
}

private fun previewUiData(): DayEntryContract.UiData = DayEntryContract.UiData(
    dayNumber = 12,
    dateLabel = "27.7.2026",
    totalDays = 30,
    energy = DayEntryContract.MetricEntry(score = 6, note = "Steadier energy through the afternoon."),
    mood = DayEntryContract.MetricEntry(score = 7, note = ""),
    sleep = DayEntryContract.MetricEntry(score = 7, note = "Falling asleep within minutes now."),
    cravings = DayEntryContract.MetricEntry(score = 5, note = ""),
    overallScore = 6,
    achievements = listOf(
        DayEntryContract.AchievementEntry(id = "1", text = "Ran 3 miles without feeling drained after"),
    ),
    meals = listOf(
        DayEntryContract.MealEntry(id = "1", label = "Meal 1", description = "Frittata with spinach and mushrooms"),
        DayEntryContract.MealEntry(id = "2", label = "Meal 2", description = "Pulled pork, roasted sweet potato", lovedIt = true),
        DayEntryContract.MealEntry(id = "3", label = "Meal 3", description = ""),
        DayEntryContract.MealEntry(id = "4", label = "Extra or Snack", description = ""),
    ),
    notes = "This is the first day it felt easy instead of like a fight.",
    isComplete = true,
)

private const val UI_MODE_NIGHT_YES = 0x20

@Preview
@Composable
private fun DayEntryScreenPreview() {
    DayEntryScreen(
        state = UiStateAware.UiState(isLoading = false, uiData = previewUiData()),
        onUiAction = {},
        onUiEventConsume = {},
    )
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun DayEntryScreenPreviewDark() {
    DayEntryScreen(
        state = UiStateAware.UiState(isLoading = false, uiData = previewUiData()),
        onUiAction = {},
        onUiEventConsume = {},
    )
}
