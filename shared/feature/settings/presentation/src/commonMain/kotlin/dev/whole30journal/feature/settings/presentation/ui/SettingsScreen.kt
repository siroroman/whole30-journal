package dev.whole30journal.feature.settings.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.whole30journal.core.designsystem.components.DSButton
import dev.whole30journal.core.designsystem.components.DSCard
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.feature.settings.presentation.generated.resources.Res
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_back_content_description
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_cancel_button
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_date_picker_done
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_decrease_duration_content_description
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_duration_subtitle
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_duration_title
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_edit_title
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_increase_duration_content_description
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_section_program
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_setup_subtitle
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_setup_title
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_start_button
import dev.whole30journal.feature.settings.presentation.generated.resources.settings_start_day_title
import dev.whole30journal.feature.settings.presentation.ui.icons.ChevronLeftIcon
import dev.whole30journal.feature.settings.presentation.ui.icons.MinusIcon
import dev.whole30journal.feature.settings.presentation.ui.icons.PlusIcon
import dev.whole30journal.feature.settings.presentation.vm.SettingsContract
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

private const val MIN_DURATION_DAYS = 1
private const val MAX_DURATION_DAYS = 90

@Composable
fun SettingsScreen(
    state: UiStateAware.UiState<SettingsContract.UiData, SettingsContract.UiEvent>,
    onUiAction: (SettingsContract.UiAction) -> Unit,
    onUiEventConsume: (SettingsContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isEditMode = state.uiData.mode == SettingsContract.Mode.Edit
    DSTheme {
        Scaffold(
            modifier = modifier,
            containerColor = DSTheme.colors.bg,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (isEditMode) {
                    SettingsTopBar(
                        isSaving = state.uiData.isSaving,
                        onBackClick = { onUiAction(SettingsContract.UiAction.OnConfirmClick) },
                    )
                }
            },
            bottomBar = {
                if (!isEditMode) {
                    SettingsFooter(
                        isSaving = state.uiData.isSaving,
                        isLoading = state.isLoading,
                        onConfirmClick = { onUiAction(SettingsContract.UiAction.OnConfirmClick) },
                    )
                }
            },
        ) { contentPadding ->
            SettingsContent(
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
    events: List<SettingsContract.UiEvent>,
    snackbarHostState: SnackbarHostState,
    onConsume: (SettingsContract.UiEvent) -> Unit,
) {
    events.forEach { event ->
        when (event) {
            is SettingsContract.UiEvent.ShowSaveError -> {
                LaunchedEffect(event) {
                    snackbarHostState.showSnackbar(event.message)
                    onConsume(event)
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(isSaving: Boolean, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.bg).statusBarsPadding()) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = DSSpacing.space6, vertical = DSSpacing.space5)) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(32.dp)
                    .clip(DSShapes.pill)
                    .clickable(role = Role.Button, enabled = !isSaving, onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                ChevronLeftIcon(
                    tint = colors.text,
                    contentDescription = stringResource(Res.string.settings_back_content_description),
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = stringResource(Res.string.settings_edit_title),
                style = DSTheme.typography.textXl,
                color = colors.text,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        HorizontalDivider(color = colors.divider)
    }
}

@Composable
private fun SettingsContent(
    uiData: SettingsContract.UiData,
    onUiAction: (SettingsContract.UiAction) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    val isSetupMode = uiData.mode == SettingsContract.Mode.Setup
    Column(
        modifier = modifier
            .padding(contentPadding)
            .let { if (isSetupMode) it.statusBarsPadding() else it }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DSSpacing.space9, vertical = DSSpacing.space7),
        verticalArrangement = Arrangement.spacedBy(DSSpacing.space9),
    ) {
        if (isSetupMode) {
            Column(verticalArrangement = Arrangement.spacedBy(DSSpacing.space3)) {
                Text(text = stringResource(Res.string.settings_setup_title), style = DSTheme.typography.text3xl, color = colors.text)
                Text(text = stringResource(Res.string.settings_setup_subtitle), style = DSTheme.typography.textBase, color = colors.textSecondary)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
            SectionHeader(text = stringResource(Res.string.settings_section_program))
            ProgramSettingsCard(
                startDateLabel = uiData.startDateLabel,
                startDateValue = uiData.startDate?.toPillLabel().orEmpty(),
                onChangeStartDateClick = { onUiAction(SettingsContract.UiAction.OnChangeStartDateClick) },
                durationDays = uiData.durationDays,
                onDurationChange = { onUiAction(SettingsContract.UiAction.OnDurationSelected(it)) },
            )
        }
    }

    if (uiData.isDatePickerVisible) {
        StartDateDialog(
            startDate = uiData.startDate,
            onDateSelect = { onUiAction(SettingsContract.UiAction.OnStartDateSelected(it)) },
            onDismiss = { onUiAction(SettingsContract.UiAction.OnDatePickerDismiss) },
        )
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = DSTheme.typography.textXs.copy(letterSpacing = 0.8.sp),
        color = DSTheme.colors.textTertiary,
        modifier = modifier.padding(start = DSSpacing.space3),
    )
}

@Composable
private fun ProgramSettingsCard(
    startDateLabel: String,
    startDateValue: String,
    onChangeStartDateClick: () -> Unit,
    durationDays: Int,
    onDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    DSCard(modifier = modifier.fillMaxWidth()) {
        SettingsRow(title = stringResource(Res.string.settings_start_day_title), subtitle = startDateLabel) {
            DateValueChip(label = startDateValue, onClick = onChangeStartDateClick)
        }
        HorizontalDivider(color = colors.divider)
        SettingsRow(
            title = stringResource(Res.string.settings_duration_title),
            subtitle = stringResource(Res.string.settings_duration_subtitle),
        ) {
            DurationStepper(
                value = durationDays,
                onDecrement = { onDurationChange((durationDays - 1).coerceAtLeast(MIN_DURATION_DAYS)) },
                onIncrement = { onDurationChange((durationDays + 1).coerceAtMost(MAX_DURATION_DAYS)) },
            )
        }
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, modifier: Modifier = Modifier, trailing: @Composable () -> Unit) {
    val colors = DSTheme.colors
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(verticalArrangement = Arrangement.spacedBy(DSSpacing.space1), modifier = Modifier.weight(1f)) {
            Text(text = title, style = DSTheme.typography.textLg, color = colors.text)
            Text(text = subtitle, style = DSTheme.typography.textSm, color = colors.textSecondary)
        }
        Spacer(modifier = Modifier.width(DSSpacing.space6))
        trailing()
    }
}

@Composable
private fun DateValueChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    Box(
        modifier = modifier
            .clip(DSShapes.md)
            .background(colors.surface2)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = DSSpacing.space6, vertical = DSSpacing.space5),
    ) {
        Text(text = label, style = DSTheme.typography.textBase, color = colors.text)
    }
}

@Composable
private fun DurationStepper(value: Int, onDecrement: () -> Unit, onIncrement: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DSSpacing.space6)) {
        StepperButton(onClick = onDecrement, enabled = value > MIN_DURATION_DAYS) { tint ->
            MinusIcon(
                tint = tint,
                contentDescription = stringResource(Res.string.settings_decrease_duration_content_description),
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = "$value",
            style = DSTheme.typography.textXl,
            color = colors.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 22.dp),
        )
        StepperButton(onClick = onIncrement, enabled = value < MAX_DURATION_DAYS) { tint ->
            PlusIcon(
                tint = tint,
                contentDescription = stringResource(Res.string.settings_increase_duration_content_description),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun StepperButton(onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier, icon: @Composable (Color) -> Unit) {
    val colors = DSTheme.colors
    val tint = if (enabled) colors.text else colors.textTertiary
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(DSShapes.pill)
            .background(colors.surface2)
            .clickable(role = Role.Button, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        icon(tint)
    }
}

@Composable
private fun SettingsFooter(isSaving: Boolean, isLoading: Boolean, onConfirmClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.bg).navigationBarsPadding()) {
        HorizontalDivider(color = colors.divider)
        Box(modifier = Modifier.padding(horizontal = DSSpacing.space9, vertical = DSSpacing.space6)) {
            DSButton(onClick = onConfirmClick, fullWidth = true, enabled = !isSaving && !isLoading) {
                Text(stringResource(Res.string.settings_start_button))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartDateDialog(startDate: LocalDate?, onDateSelect: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val colors = DSTheme.colors
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate?.toUtcMillis())
    val accentButtonColors = ButtonDefaults.textButtonColors(contentColor = colors.accent)
    val datePickerColors = DatePickerDefaults.colors(
        containerColor = colors.surface,
        titleContentColor = colors.textSecondary,
        headlineContentColor = colors.text,
        weekdayContentColor = colors.textSecondary,
        subheadContentColor = colors.textSecondary,
        navigationContentColor = colors.text,
        yearContentColor = colors.text,
        disabledYearContentColor = colors.textTertiary,
        currentYearContentColor = colors.accent,
        selectedYearContentColor = colors.accentOn,
        disabledSelectedYearContentColor = colors.textTertiary,
        selectedYearContainerColor = colors.accent,
        disabledSelectedYearContainerColor = colors.surface2,
        dayContentColor = colors.text,
        disabledDayContentColor = colors.textTertiary,
        selectedDayContentColor = colors.accentOn,
        disabledSelectedDayContentColor = colors.textTertiary,
        selectedDayContainerColor = colors.accent,
        disabledSelectedDayContainerColor = colors.surface2,
        todayContentColor = colors.accent,
        todayDateBorderColor = colors.accent,
        dayInSelectionRangeContentColor = colors.text,
        dayInSelectionRangeContainerColor = colors.accentTint,
        dividerColor = colors.divider,
        dateTextFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.text,
            unfocusedTextColor = colors.text,
            cursorColor = colors.accent,
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.divider,
            focusedLabelColor = colors.accent,
            unfocusedLabelColor = colors.textSecondary,
            focusedPlaceholderColor = colors.textTertiary,
            unfocusedPlaceholderColor = colors.textTertiary,
            focusedSupportingTextColor = colors.textSecondary,
            unfocusedSupportingTextColor = colors.textSecondary,
            errorTextColor = colors.scoreLow,
            errorBorderColor = colors.scoreLow,
            errorLabelColor = colors.scoreLow,
            errorSupportingTextColor = colors.scoreLow,
        ),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { datePickerState.selectedDateMillis?.let { onDateSelect(it.toLocalDate()) } ?: onDismiss() },
                colors = accentButtonColors,
            ) {
                Text(stringResource(Res.string.settings_date_picker_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = accentButtonColors) {
                Text(stringResource(Res.string.settings_cancel_button))
            }
        },
        shape = DSShapes.xl,
        colors = datePickerColors,
    ) {
        DatePicker(state = datePickerState, colors = datePickerColors)
    }
}

private const val MILLIS_PER_DAY = 86_400_000L

private fun LocalDate.toUtcMillis(): Long = this.toEpochDays() * MILLIS_PER_DAY

private fun Long.toLocalDate(): LocalDate = LocalDate.fromEpochDays(this / MILLIS_PER_DAY)

private fun LocalDate.toPillLabel(): String = "$day.${month.ordinal + 1}.$year"

private fun previewUiData(mode: SettingsContract.Mode): SettingsContract.UiData = SettingsContract.UiData(
    mode = mode,
    startDate = LocalDate(2026, 7, 16),
    startDateLabel = "Thursday 16.7.2026",
    durationDays = 30,
    endDateLabel = "14.8.2026",
)

private const val UI_MODE_NIGHT_YES = 0x20

@Preview
@Composable
private fun SettingsScreenSetupPreview() {
    SettingsScreen(
        state = UiStateAware.UiState(isLoading = false, uiData = previewUiData(SettingsContract.Mode.Setup)),
        onUiAction = {},
        onUiEventConsume = {},
    )
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenEditPreviewDark() {
    SettingsScreen(
        state = UiStateAware.UiState(isLoading = false, uiData = previewUiData(SettingsContract.Mode.Edit)),
        onUiAction = {},
        onUiEventConsume = {},
    )
}
