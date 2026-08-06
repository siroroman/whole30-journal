package dev.whole30journal.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.core.designsystem.theme.scoreColor

@Composable
fun DSProgressRing(
    score: Int?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    stroke: Dp = 5.dp,
    label: String? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = DSTheme.colors
    val ringColor by animateColorAsState(targetValue = colors.scoreColor(score), label = "progressRingColor")
    val targetFraction = score?.let { (it / 10f).coerceIn(0f, 1f) } ?: 0f
    val fraction by animateFloatAsState(targetValue = targetFraction, label = "progressRingFraction")
    val valueTextStyle = when {
        size >= 100.dp -> DSTheme.typography.text4xl
        size >= 56.dp -> DSTheme.typography.text2xl
        else -> DSTheme.typography.textBase
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(size)) {
                val strokePx = stroke.toPx()
                val diameter = this.size.minDimension - strokePx
                val arcTopLeft = Offset(strokePx / 2f, strokePx / 2f)
                val arcSize = Size(diameter, diameter)
                drawArc(
                    color = colors.track,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
                if (fraction > 0f) {
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * fraction,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    )
                }
            }
            Text(
                text = score?.toString() ?: "–",
                style = valueTextStyle.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.text,
                textAlign = TextAlign.Center,
            )
        }
        if (label != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                icon?.invoke()
                Text(
                    text = label.uppercase(),
                    style = DSTheme.typography.text2xs.copy(letterSpacing = 0.08.em, fontWeight = FontWeight.Normal),
                    color = colors.textTertiary,
                )
            }
        }
    }
}

@Preview
@Composable
private fun DSProgressRingPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DSProgressRing(score = 7, size = 150.dp, label = "Overall")
                DSProgressRing(score = 4, size = 48.dp)
                DSProgressRing(score = null, size = 48.dp)
            }
        }
    }
}

@Preview
@Composable
private fun DSProgressRingPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DSProgressRing(score = 7, size = 150.dp, label = "Overall")
                DSProgressRing(score = 4, size = 48.dp)
                DSProgressRing(score = null, size = 48.dp)
            }
        }
    }
}
