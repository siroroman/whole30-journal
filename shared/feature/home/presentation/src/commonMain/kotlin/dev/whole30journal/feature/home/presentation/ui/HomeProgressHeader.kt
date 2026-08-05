package dev.whole30journal.feature.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.components.Whole30ProgressBar
import dev.whole30journal.core.designsystem.theme.Whole30Shapes
import dev.whole30journal.core.designsystem.theme.Whole30Spacing
import dev.whole30journal.core.designsystem.theme.Whole30Theme
import dev.whole30journal.feature.home.presentation.generated.resources.Res
import dev.whole30journal.feature.home.presentation.generated.resources.home_today_label
import dev.whole30journal.feature.home.presentation.generated.resources.home_wordmark
import dev.whole30journal.feature.home.presentation.ui.icons.LeafIcon
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeProgressHeader(
    currentDay: Int,
    totalDays: Int,
    progressPercent: Int,
    progressStartLabel: String,
    progressEndLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Whole30Spacing.space8)) {
        HomeHeader(currentDay = currentDay, totalDays = totalDays)
        Whole30ProgressBar(
            value = currentDay,
            modifier = Modifier.fillMaxWidth(),
            max = totalDays,
            labelLeft = progressStartLabel,
            labelCenter = "${stringResource(Res.string.home_today_label)} · $progressPercent%",
            labelRight = progressEndLabel,
        )
    }
}

@Composable
private fun HomeHeader(currentDay: Int, totalDays: Int, modifier: Modifier = Modifier) {
    val colors = Whole30Theme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Whole30Spacing.space4),
        ) {
            Box(
                modifier = Modifier.size(28.dp).clip(Whole30Shapes.sm).background(colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                LeafIcon(tint = colors.accentOn, modifier = Modifier.size(16.dp))
            }
            Text(text = stringResource(Res.string.home_wordmark), style = Whole30Theme.typography.textXl, color = colors.text)
        }
        Row(
            modifier = Modifier
                .clip(Whole30Shapes.pill)
                .background(colors.surface)
                .padding(horizontal = Whole30Spacing.space5, vertical = Whole30Spacing.space3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$currentDay",
                style = Whole30Theme.typography.textBase.copy(fontWeight = FontWeight.Bold),
                color = colors.accent,
            )
            Text(text = " / $totalDays", style = Whole30Theme.typography.textSm, color = colors.textSecondary)
        }
    }
}

@Preview
@Composable
private fun HomeProgressHeaderPreviewLight() {
    Whole30Theme(darkTheme = false) {
        Surface(color = Whole30Theme.colors.bg) {
            HomeProgressHeader(
                currentDay = 12,
                totalDays = 30,
                progressPercent = 40,
                progressStartLabel = "25.7.2026",
                progressEndLabel = "23.8.2026",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun HomeProgressHeaderPreviewDark() {
    Whole30Theme(darkTheme = true) {
        Surface(color = Whole30Theme.colors.bg) {
            HomeProgressHeader(
                currentDay = 12,
                totalDays = 30,
                progressPercent = 40,
                progressStartLabel = "25.7.2026",
                progressEndLabel = "23.8.2026",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
