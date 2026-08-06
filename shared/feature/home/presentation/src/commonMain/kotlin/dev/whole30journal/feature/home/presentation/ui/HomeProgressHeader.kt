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
import dev.whole30journal.core.designsystem.components.DSProgressBar
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
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
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(DSSpacing.space8)) {
        HomeHeader(currentDay = currentDay, totalDays = totalDays)
        DSProgressBar(
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
    val colors = DSTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.space4),
        ) {
            Box(
                modifier = Modifier.size(28.dp).clip(DSShapes.sm).background(colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                LeafIcon(tint = colors.accentOn, modifier = Modifier.size(16.dp))
            }
            Text(text = stringResource(Res.string.home_wordmark), style = DSTheme.typography.textXl, color = colors.text)
        }
        Row(
            modifier = Modifier
                .clip(DSShapes.pill)
                .background(colors.surface)
                .padding(horizontal = DSSpacing.space5, vertical = DSSpacing.space3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$currentDay",
                style = DSTheme.typography.textBase.copy(fontWeight = FontWeight.Bold),
                color = colors.accent,
            )
            Text(text = " / $totalDays", style = DSTheme.typography.textSm, color = colors.textSecondary)
        }
    }
}

@Preview
@Composable
private fun HomeProgressHeaderPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
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
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
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
