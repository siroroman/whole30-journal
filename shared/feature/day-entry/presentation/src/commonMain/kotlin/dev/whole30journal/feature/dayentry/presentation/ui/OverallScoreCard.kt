package dev.whole30journal.feature.dayentry.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.whole30journal.core.designsystem.components.DSCard
import dev.whole30journal.core.designsystem.components.DSScoreDots
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.core.designsystem.theme.scoreColor
import dev.whole30journal.feature.dayentry.presentation.generated.resources.Res
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_overall_caption
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_overall_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun OverallScoreCard(score: Int?, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    DSCard(modifier = modifier.fillMaxWidth()) {
        Text(text = stringResource(Res.string.day_entry_overall_title), style = DSTheme.typography.textMd, color = colors.text)
        Text(text = score?.toString() ?: "–", style = DSTheme.typography.text2xl, color = colors.scoreColor(score))
        DSScoreDots(score = score, enabled = false)
        Text(text = stringResource(Res.string.day_entry_overall_caption), style = DSTheme.typography.textSm, color = colors.textTertiary)
    }
}

@Preview
@Composable
private fun OverallScoreCardPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            OverallScoreCard(score = 6, modifier = Modifier.padding(DSSpacing.space7))
        }
    }
}

@Preview
@Composable
private fun OverallScoreCardPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            OverallScoreCard(score = null, modifier = Modifier.padding(DSSpacing.space7))
        }
    }
}
