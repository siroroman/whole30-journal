package dev.whole30journal.feature.daydetail.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.whole30journal.core.designsystem.components.DSButton
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.daydetail.presentation.generated.resources.Res
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_empty_message
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_empty_title
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_log_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun DayDetailEmptyState(dayNumber: Int, onLogClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = DSSpacing.space12),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DSSpacing.space6),
    ) {
        Text(
            text = stringResource(Res.string.day_detail_empty_title),
            style = DSTheme.typography.textMd.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.day_detail_empty_message, dayNumber),
            style = DSTheme.typography.textSm,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
        )
        DSButton(onClick = onLogClick) {
            Text(stringResource(Res.string.day_detail_log_button, dayNumber))
        }
    }
}

@Preview
@Composable
private fun DayDetailEmptyStatePreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            DayDetailEmptyState(dayNumber = 18, onLogClick = {}, modifier = Modifier.padding(DSSpacing.space7))
        }
    }
}

@Preview
@Composable
private fun DayDetailEmptyStatePreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            DayDetailEmptyState(dayNumber = 18, onLogClick = {}, modifier = Modifier.padding(DSSpacing.space7))
        }
    }
}
