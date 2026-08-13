package dev.whole30journal.feature.daydetail.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.whole30journal.core.designsystem.components.DSCard
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.daydetail.presentation.generated.resources.Res
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_notes_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotesSummaryCard(notes: String, modifier: Modifier = Modifier) {
    if (notes.isBlank()) return
    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
        Text(text = stringResource(Res.string.day_detail_notes_title), style = DSTheme.typography.textLg, color = colors.text)
        DSCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = notes, style = DSTheme.typography.textSm, color = colors.text)
        }
    }
}

@Preview
@Composable
private fun NotesSummaryCardPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            NotesSummaryCard(
                notes = "Twelve days in and this finally feels like a routine, not a restriction.",
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}

@Preview
@Composable
private fun NotesSummaryCardPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            NotesSummaryCard(
                notes = "Twelve days in and this finally feels like a routine, not a restriction.",
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}
