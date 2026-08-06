package dev.whole30journal.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.theme.DSTheme

@Composable
fun DSProgressBar(
    value: Int,
    modifier: Modifier = Modifier,
    max: Int = 30,
    labelLeft: String? = null,
    labelCenter: String? = null,
    labelRight: String? = null,
) {
    val colors = DSTheme.colors
    val fraction = if (max <= 0) 0f else (value.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.divider),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(colors.accent),
            )
        }
        if (labelLeft != null || labelCenter != null || labelRight != null) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = labelLeft.orEmpty(), style = DSTheme.typography.text2xs, color = colors.textSecondary)
                Text(text = labelCenter.orEmpty(), style = DSTheme.typography.text2xs, color = colors.textSecondary)
                Text(text = labelRight.orEmpty(), style = DSTheme.typography.text2xs, color = colors.textSecondary)
            }
        }
    }
}

@Preview
@Composable
private fun DSProgressBarPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            Box(modifier = Modifier.padding(16.dp)) {
                DSProgressBar(value = 12, max = 30, labelLeft = "Day 1", labelCenter = "Day 12 · 40%", labelRight = "Day 30")
            }
        }
    }
}

@Preview
@Composable
private fun DSProgressBarPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            Box(modifier = Modifier.padding(16.dp)) {
                DSProgressBar(value = 12, max = 30, labelLeft = "Day 1", labelCenter = "Day 12 · 40%", labelRight = "Day 30")
            }
        }
    }
}
