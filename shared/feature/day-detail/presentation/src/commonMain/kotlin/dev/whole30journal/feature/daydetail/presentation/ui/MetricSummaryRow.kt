package dev.whole30journal.feature.daydetail.presentation.ui

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
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.components.DSCard
import dev.whole30journal.core.designsystem.components.DSProgressRing
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.daydetail.presentation.vm.DayDetailContract

private val RingSize = 48.dp
private val RingStroke = 4.5.dp

@Composable
fun MetricSummaryRow(summary: DayDetailContract.MetricSummary, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    DSCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(DSSpacing.space1)) {
                Text(text = summary.title, style = DSTheme.typography.textMd, color = colors.text)
                if (summary.note.isNotBlank()) {
                    Text(text = summary.note, style = DSTheme.typography.textSm, color = colors.textSecondary)
                }
            }
            DSProgressRing(score = summary.score, size = RingSize, stroke = RingStroke)
        }
    }
}

@Preview
@Composable
private fun MetricSummaryRowPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            Column(modifier = Modifier.padding(DSSpacing.space7), verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
                MetricSummaryRow(
                    summary = DayDetailContract.MetricSummary(
                        title = "Energy",
                        note = "Exhausted by 3pm, pounding headache.",
                        score = 3,
                    ),
                )
                MetricSummaryRow(summary = DayDetailContract.MetricSummary(title = "Mood", note = "", score = null))
            }
        }
    }
}

@Preview
@Composable
private fun MetricSummaryRowPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            Column(modifier = Modifier.padding(DSSpacing.space7)) {
                MetricSummaryRow(
                    summary = DayDetailContract.MetricSummary(title = "Sleep quality", note = "Fell asleep faster.", score = 8),
                )
            }
        }
    }
}
