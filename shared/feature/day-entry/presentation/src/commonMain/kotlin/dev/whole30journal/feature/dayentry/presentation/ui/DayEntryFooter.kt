package dev.whole30journal.feature.dayentry.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.whole30journal.core.designsystem.components.DSButton
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.dayentry.presentation.generated.resources.Res
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_save_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun DayEntryFooter(
    dayNumber: Int,
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.bg).navigationBarsPadding()) {
        HorizontalDivider(color = colors.divider)
        Column(modifier = Modifier.padding(horizontal = DSSpacing.space20, vertical = DSSpacing.space14)) {
            DSButton(onClick = onSaveClick, fullWidth = true, enabled = !isSaving) {
                Text(stringResource(Res.string.day_entry_save_button, dayNumber))
            }
        }
    }
}

@Preview
@Composable
private fun DayEntryFooterPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            DayEntryFooter(dayNumber = 12, isSaving = false, onSaveClick = {})
        }
    }
}

@Preview
@Composable
private fun DayEntryFooterPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            DayEntryFooter(dayNumber = 12, isSaving = false, onSaveClick = {})
        }
    }
}
