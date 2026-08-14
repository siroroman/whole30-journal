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
import dev.whole30journal.feature.daydetail.presentation.generated.resources.Res
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_overall_caption
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_overall_title
import org.jetbrains.compose.resources.stringResource

private val RingSize = 48.dp
private val RingStroke = 4.5.dp

@Composable
fun OverallScoreSummaryCard(score: Int?, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    DSCard(highlighted = true, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(DSSpacing.space1)) {
                Text(text = stringResource(Res.string.day_detail_overall_title), style = DSTheme.typography.textMd, color = colors.text)
                Text(text = stringResource(Res.string.day_detail_overall_caption), style = DSTheme.typography.textSm, color = colors.textSecondary)
            }
            DSProgressRing(score = score, size = RingSize, stroke = RingStroke)
        }
    }
}

@Preview
@Composable
private fun OverallScoreSummaryCardPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            OverallScoreSummaryCard(score = 8, modifier = Modifier.padding(DSSpacing.space7))
        }
    }
}

@Preview
@Composable
private fun OverallScoreSummaryCardPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            OverallScoreSummaryCard(score = null, modifier = Modifier.padding(DSSpacing.space7))
        }
    }
}
