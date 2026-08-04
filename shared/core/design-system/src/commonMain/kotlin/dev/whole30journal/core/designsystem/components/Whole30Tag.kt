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
import dev.whole30journal.core.designsystem.theme.Whole30Shapes
import dev.whole30journal.core.designsystem.theme.Whole30Theme

@Composable
fun Whole30Tag(
    text: String,
    modifier: Modifier = Modifier,
    tone: Whole30TagTone = Whole30TagTone.Accent,
) {
    val colors = Whole30Theme.colors
    val (background, contentColor) = when (tone) {
        Whole30TagTone.Accent -> colors.accentTint to colors.accent
        Whole30TagTone.Neutral -> colors.surface2 to colors.textSecondary
    }
    Surface(
        modifier = modifier,
        color = background,
        contentColor = contentColor,
        shape = Whole30Shapes.pill,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = Whole30Theme.typography.textXs,
        )
    }
}

@Preview
@Composable
private fun Whole30TagPreviewLight() {
    Whole30Theme(darkTheme = false) {
        Surface(color = Whole30Theme.colors.bg) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Whole30Tag(text = "Day completed", tone = Whole30TagTone.Accent)
                Whole30Tag(text = "12 / 30", tone = Whole30TagTone.Neutral)
            }
        }
    }
}

@Preview
@Composable
private fun Whole30TagPreviewDark() {
    Whole30Theme(darkTheme = true) {
        Surface(color = Whole30Theme.colors.bg) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Whole30Tag(text = "Day completed", tone = Whole30TagTone.Accent)
                Whole30Tag(text = "12 / 30", tone = Whole30TagTone.Neutral)
            }
        }
    }
}
