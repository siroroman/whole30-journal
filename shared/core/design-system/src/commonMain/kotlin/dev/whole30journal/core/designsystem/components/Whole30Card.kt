package dev.whole30journal.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.theme.Whole30Shapes
import dev.whole30journal.core.designsystem.theme.Whole30Spacing
import dev.whole30journal.core.designsystem.theme.Whole30Theme

@Composable
fun Whole30Card(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    contentPadding: Dp = Whole30Spacing.space7,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = Whole30Theme.colors
    Surface(
        modifier = modifier,
        color = colors.surface,
        contentColor = colors.text,
        shape = Whole30Shapes.xl,
        border = if (highlighted) BorderStroke(1.5.dp, colors.accent) else null,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(Whole30Spacing.space5),
            content = content,
        )
    }
}

@Preview
@Composable
private fun Whole30CardPreviewLight() {
    Whole30Theme(darkTheme = false) {
        Surface(color = Whole30Theme.colors.bg) {
            Column(
                modifier = Modifier.padding(Whole30Spacing.space7),
                verticalArrangement = Arrangement.spacedBy(Whole30Spacing.space5),
            ) {
                Whole30Card { Text("Today", style = Whole30Theme.typography.textMd) }
                Whole30Card(highlighted = true) { Text("Overall score", style = Whole30Theme.typography.textMd) }
            }
        }
    }
}

@Preview
@Composable
private fun Whole30CardPreviewDark() {
    Whole30Theme(darkTheme = true) {
        Surface(color = Whole30Theme.colors.bg) {
            Column(
                modifier = Modifier.padding(Whole30Spacing.space7),
                verticalArrangement = Arrangement.spacedBy(Whole30Spacing.space5),
            ) {
                Whole30Card { Text("Today", style = Whole30Theme.typography.textMd) }
                Whole30Card(highlighted = true) { Text("Overall score", style = Whole30Theme.typography.textMd) }
            }
        }
    }
}
