package dev.whole30journal.feature.daydetail.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.whole30journal.core.designsystem.components.DSCard
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.daydetail.presentation.generated.resources.Res
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_nsv_title
import org.jetbrains.compose.resources.stringResource

private const val CHECKMARK = "✓"

@Composable
fun AchievementsSummaryList(achievements: List<String>, modifier: Modifier = Modifier) {
    if (achievements.isEmpty()) return
    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
        Text(text = stringResource(Res.string.day_detail_nsv_title), style = DSTheme.typography.textLg, color = colors.text)
        DSCard(modifier = Modifier.fillMaxWidth(), contentPadding = DSSpacing.space6) {
            achievements.forEachIndexed { index, text ->
                Row(horizontalArrangement = Arrangement.spacedBy(DSSpacing.space3)) {
                    Text(text = CHECKMARK, style = DSTheme.typography.textSm, color = colors.accent)
                    Text(text = text, style = DSTheme.typography.textSm, color = colors.text)
                }
                if (index != achievements.lastIndex) HorizontalDivider(color = colors.divider)
            }
        }
    }
}

@Preview
@Composable
private fun AchievementsSummaryListPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            AchievementsSummaryList(
                achievements = listOf(
                    "Cooked a full dinner from scratch",
                    "Read every ingredient label at the store",
                    "Didn't quit on day one",
                ),
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}

@Preview
@Composable
private fun AchievementsSummaryListPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            AchievementsSummaryList(
                achievements = listOf("Cooked a full dinner from scratch"),
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}
