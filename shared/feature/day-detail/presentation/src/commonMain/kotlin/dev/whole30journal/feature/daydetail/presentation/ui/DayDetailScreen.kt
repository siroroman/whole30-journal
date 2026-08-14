package dev.whole30journal.feature.daydetail.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import dev.whole30journal.core.designsystem.components.DSTag
import dev.whole30journal.core.designsystem.components.DSTagTone
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.feature.daydetail.presentation.generated.resources.Res
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_back_content_description
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_complete_tag
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_edit_button
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_not_complete_tag
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_title
import dev.whole30journal.feature.daydetail.presentation.ui.icons.ChevronLeftIcon
import dev.whole30journal.feature.daydetail.presentation.vm.DayDetailContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun DayDetailScreen(
    state: UiStateAware.UiState<DayDetailContract.UiData, DayDetailContract.UiEvent>,
    onUiAction: (DayDetailContract.UiAction) -> Unit,
    onUiEventConsume: (DayDetailContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    DSTheme {
        Scaffold(
            modifier = modifier,
            containerColor = DSTheme.colors.bg,
            topBar = {
                DayDetailTopBar(
                    dayNumber = state.uiData.dayNumber,
                    dateLabel = state.uiData.dateLabel,
                    onBackClick = { onUiAction(DayDetailContract.UiAction.OnBackClick) },
                    onEditClick = { onUiAction(DayDetailContract.UiAction.OnEditClick) },
                )
            },
        ) { contentPadding ->
            DayDetailContent(
                uiData = state.uiData,
                onUiAction = onUiAction,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize(),
            )
            HandleUiEvents(events = state.uiEvents, onConsume = onUiEventConsume)
        }
    }
}

@Composable
private fun HandleUiEvents(events: List<DayDetailContract.UiEvent>, onConsume: (DayDetailContract.UiEvent) -> Unit) {
    // DayDetailContract.UiEvent has no cases yet - kept for signature parity with other screens.
    events.forEach { event ->
        LaunchedEffect(event) { onConsume(event) }
    }
}

@Composable
private fun DayDetailTopBar(
    dayNumber: Int,
    dateLabel: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.bg).statusBarsPadding()) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = DSSpacing.space9, vertical = DSSpacing.space6)) {
            ChevronLeftIcon(
                tint = colors.textSecondary,
                contentDescription = stringResource(Res.string.day_detail_back_content_description),
                modifier = Modifier.align(Alignment.CenterStart).clickable(onClick = onBackClick),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                Text(text = stringResource(Res.string.day_detail_title, dayNumber), style = DSTheme.typography.textMd, color = colors.text)
                Text(text = dateLabel, style = DSTheme.typography.textXs, color = colors.textTertiary)
            }
            Text(
                text = stringResource(Res.string.day_detail_edit_button),
                style = DSTheme.typography.textLg.copy(fontWeight = FontWeight.Bold),
                color = colors.accent,
                modifier = Modifier.align(Alignment.CenterEnd).clickable(onClick = onEditClick),
            )
        }
        HorizontalDivider(color = colors.divider)
    }
}

@Composable
private fun DayDetailContent(
    uiData: DayDetailContract.UiData,
    onUiAction: (DayDetailContract.UiAction) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = DSSpacing.space7, vertical = DSSpacing.space7),
        verticalArrangement = Arrangement.spacedBy(DSSpacing.space8),
    ) {
        if (uiData.hasEntry) {
            DSTag(
                text = if (uiData.isComplete) {
                    stringResource(Res.string.day_detail_complete_tag)
                } else {
                    stringResource(Res.string.day_detail_not_complete_tag)
                },
                tone = if (uiData.isComplete) DSTagTone.Accent else DSTagTone.Neutral,
            )
            OverallScoreSummaryCard(score = uiData.overallScore)
            Column(verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
                uiData.metrics.forEach { summary -> MetricSummaryRow(summary = summary) }
            }
            AchievementsSummaryList(achievements = uiData.achievements)
            MealsSummarySection(meals = uiData.meals)
            NotesSummaryCard(notes = uiData.notes)
        } else {
            DayDetailEmptyState(
                dayNumber = uiData.dayNumber,
                onLogClick = { onUiAction(DayDetailContract.UiAction.OnEditClick) },
            )
        }
    }
}

private fun previewUiData(): DayDetailContract.UiData = DayDetailContract.UiData(
    dayNumber = 12,
    dateLabel = "27.7.2026",
    hasEntry = true,
    isComplete = true,
    overallScore = 8,
    metrics = listOf(
        DayDetailContract.MetricSummary(title = "Energy", note = "Great energy all day, even woke up early on my own.", score = 8),
        DayDetailContract.MetricSummary(title = "Mood", note = "Feeling optimistic and calm.", score = 8),
        DayDetailContract.MetricSummary(title = "Sleep quality", note = "Best sleep of the month.", score = 8),
        DayDetailContract.MetricSummary(title = "Cravings", note = "Passed a bakery without a second thought.", score = 6),
    ),
    achievements = listOf("Walked past a bakery and felt nothing", "More confident in my clothes"),
    meals = listOf(
        DayDetailContract.MealSummary(id = "1", label = "Meal 1", description = "Sweet potato hash with chorizo", photoToken = null, lovedIt = true),
        DayDetailContract.MealSummary(id = "2", label = "Meal 2", description = "Grilled chicken salad", photoToken = null, lovedIt = false),
    ),
    notes = "Almost done with the 30 days and it genuinely feels sustainable now.",
)

private fun previewEmptyUiData(): DayDetailContract.UiData = DayDetailContract.UiData(dayNumber = 18, dateLabel = "2.8.2026", hasEntry = false)

private const val UI_MODE_NIGHT_YES = 0x20

@Preview
@Composable
private fun DayDetailScreenPreview() {
    DayDetailScreen(
        state = UiStateAware.UiState(isLoading = false, uiData = previewUiData()),
        onUiAction = {},
        onUiEventConsume = {},
    )
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun DayDetailScreenPreviewDark() {
    DayDetailScreen(
        state = UiStateAware.UiState(isLoading = false, uiData = previewUiData()),
        onUiAction = {},
        onUiEventConsume = {},
    )
}

@Preview
@Composable
private fun DayDetailScreenEmptyPreview() {
    DayDetailScreen(
        state = UiStateAware.UiState(isLoading = false, uiData = previewEmptyUiData()),
        onUiAction = {},
        onUiEventConsume = {},
    )
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun DayDetailScreenEmptyPreviewDark() {
    DayDetailScreen(
        state = UiStateAware.UiState(isLoading = false, uiData = previewEmptyUiData()),
        onUiAction = {},
        onUiEventConsume = {},
    )
}
