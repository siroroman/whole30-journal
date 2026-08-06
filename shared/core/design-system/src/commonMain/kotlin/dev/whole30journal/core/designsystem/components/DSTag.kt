package dev.whole30journal.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSTheme

@Composable
fun DSTag(
    text: String,
    modifier: Modifier = Modifier,
    tone: DSTagTone = DSTagTone.Accent,
) {
    val colors = DSTheme.colors
    val (background, contentColor) = when (tone) {
        DSTagTone.Accent -> colors.accentTint to colors.accent
        DSTagTone.Neutral -> colors.surface2 to colors.textSecondary
    }
    Surface(
        modifier = modifier,
        color = background,
        contentColor = contentColor,
        shape = DSShapes.pill,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = DSTheme.typography.textXs,
        )
    }
}

@Preview
@Composable
private fun DSTagPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DSTag(text = "Day completed", tone = DSTagTone.Accent)
                DSTag(text = "12 / 30", tone = DSTagTone.Neutral)
            }
        }
    }
}

@Preview
@Composable
private fun DSTagPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DSTag(text = "Day completed", tone = DSTagTone.Accent)
                DSTag(text = "12 / 30", tone = DSTagTone.Neutral)
            }
        }
    }
}
