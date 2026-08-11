package dev.whole30journal.feature.dayentry.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.components.DSButton
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.dayentry.presentation.generated.resources.Res
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_complete_toggle_label
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_save_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun DayEntryFooter(
    dayNumber: Int,
    isComplete: Boolean,
    isSaving: Boolean,
    onCompleteToggle: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.bg).navigationBarsPadding()) {
        HorizontalDivider(color = colors.divider)
        Column(
            modifier = Modifier.padding(horizontal = DSSpacing.space9, vertical = DSSpacing.space6),
            verticalArrangement = Arrangement.spacedBy(DSSpacing.space5),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onCompleteToggle),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.day_entry_complete_toggle_label),
                    style = DSTheme.typography.textBase.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.text,
                )
                CompleteToggleTrack(checked = isComplete)
            }
            DSButton(onClick = onSaveClick, fullWidth = true, enabled = !isSaving) {
                Text(stringResource(Res.string.day_entry_save_button, dayNumber))
            }
        }
    }
}

@Composable
private fun CompleteToggleTrack(checked: Boolean, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    val trackColor by animateColorAsState(if (checked) colors.accent else colors.track, label = "completeTrackColor")
    val thumbOffset by animateDpAsState(if (checked) 20.dp else 2.dp, label = "completeThumbOffset")
    Box(modifier = modifier.size(width = 44.dp, height = 26.dp).clip(DSShapes.pill).background(trackColor)) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset, top = 2.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Preview
@Composable
private fun DayEntryFooterPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            DayEntryFooter(dayNumber = 12, isComplete = true, isSaving = false, onCompleteToggle = {}, onSaveClick = {})
        }
    }
}

@Preview
@Composable
private fun DayEntryFooterPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            DayEntryFooter(dayNumber = 12, isComplete = false, isSaving = false, onCompleteToggle = {}, onSaveClick = {})
        }
    }
}
