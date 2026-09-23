package dev.whole30journal.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme

@Composable
fun DSCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    contentPadding: Dp = DSSpacing.space16,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = DSTheme.colors
    val shape = DSShapes.xl
    val clickModifier = if (onClick != null) Modifier.clip(shape).clickable(role = Role.Button, onClick = onClick) else Modifier
    Surface(
        modifier = modifier.then(clickModifier),
        color = colors.surface,
        contentColor = colors.text,
        shape = shape,
        border = if (highlighted) BorderStroke(1.5.dp, colors.accent) else null,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(DSSpacing.space12),
            content = content,
        )
    }
}

@Preview
@Composable
private fun DSCardPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            Column(
                modifier = Modifier.padding(DSSpacing.space16),
                verticalArrangement = Arrangement.spacedBy(DSSpacing.space12),
            ) {
                DSCard { Text("Today", style = DSTheme.typography.textMd) }
                DSCard(highlighted = true) { Text("Overall score", style = DSTheme.typography.textMd) }
            }
        }
    }
}

@Preview
@Composable
private fun DSCardPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            Column(
                modifier = Modifier.padding(DSSpacing.space16),
                verticalArrangement = Arrangement.spacedBy(DSSpacing.space12),
            ) {
                DSCard { Text("Today", style = DSTheme.typography.textMd) }
                DSCard(highlighted = true) { Text("Overall score", style = DSTheme.typography.textMd) }
            }
        }
    }
}
