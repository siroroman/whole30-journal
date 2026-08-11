package dev.whole30journal.feature.dayentry.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.whole30journal.core.designsystem.components.DSCard
import dev.whole30journal.core.designsystem.components.DSScoreDots
import dev.whole30journal.core.designsystem.components.DSTextField
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.core.designsystem.theme.scoreColor

@Composable
fun MetricScoreCard(
    title: String,
    lowLabel: String,
    highLabel: String,
    score: Int?,
    note: String,
    notePlaceholder: String,
    onScoreChange: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    DSCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(text = title, style = DSTheme.typography.textMd, color = colors.text)
            Text(
                text = score?.toString() ?: "–",
                style = DSTheme.typography.text2xl,
                color = colors.scoreColor(score),
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = lowLabel, style = DSTheme.typography.text2xs, color = colors.textTertiary)
            Text(text = highLabel, style = DSTheme.typography.text2xs, color = colors.textTertiary)
        }
        DSScoreDots(score = score, onScoreChange = onScoreChange)
        DSTextField(
            value = note,
            onValueChange = onNoteChange,
            placeholder = notePlaceholder,
            singleLine = false,
            minLines = 2,
        )
    }
}

@Preview
@Composable
private fun MetricScoreCardPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            Column(modifier = Modifier.padding(DSSpacing.space7), verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
                MetricScoreCard(
                    title = "Energy",
                    lowLabel = "Worst",
                    highLabel = "Best",
                    score = 6,
                    note = "Steadier energy through the afternoon.",
                    notePlaceholder = "Note",
                    onScoreChange = {},
                    onNoteChange = {},
                )
                MetricScoreCard(
                    title = "Mood",
                    lowLabel = "Low",
                    highLabel = "Great",
                    score = null,
                    note = "",
                    notePlaceholder = "Note",
                    onScoreChange = {},
                    onNoteChange = {},
                )
            }
        }
    }
}

@Preview
@Composable
private fun MetricScoreCardPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            Column(modifier = Modifier.padding(DSSpacing.space7), verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
                MetricScoreCard(
                    title = "Energy",
                    lowLabel = "Worst",
                    highLabel = "Best",
                    score = 6,
                    note = "Steadier energy through the afternoon.",
                    notePlaceholder = "Note",
                    onScoreChange = {},
                    onNoteChange = {},
                )
            }
        }
    }
}
