package dev.whole30journal.feature.dayentry.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.dayentry.presentation.ui.icons.PlusIcon

@Composable
fun AddEntryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .dashedBorder(colors.divider, DSShapes.lg)
            .clickable(onClick = onClick)
            .padding(DSSpacing.space6),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlusIcon(tint = colors.textSecondary, modifier = Modifier.size(16.dp))
        Text(
            text = text,
            style = DSTheme.typography.textBase,
            color = colors.textSecondary,
            modifier = Modifier.padding(start = DSSpacing.space3),
        )
    }
}

internal fun Modifier.dashedBorder(color: Color, shape: Shape, width: Dp = 1.5.dp): Modifier = drawWithContent {
    drawContent()
    drawOutline(
        outline = shape.createOutline(size, layoutDirection, this),
        color = color,
        style = Stroke(width = width.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))),
    )
}
