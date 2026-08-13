package dev.whole30journal.android

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.whole30journal.core.designsystem.theme.dsDarkColors
import dev.whole30journal.core.designsystem.theme.dsFontFamily

@Composable
fun SplashScreen() {
    val colors = dsDarkColors()
    val fontFamily = dsFontFamily()

    val infiniteTransition = rememberInfiniteTransition(label = "splash-pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "splash-pulse-alpha",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(52.dp)) {
                    val scale = size.width / 24f
                    fun s(v: Float) = v * scale

                    val stroke = Stroke(width = 2.2f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)

                    val stem = Path().apply {
                        moveTo(s(12f), s(2f))
                        lineTo(s(12f), s(8f))
                    }
                    val topArc = Path().apply {
                        moveTo(s(8f), s(4f))
                        cubicTo(s(8f), s(8f), s(9f), s(10f), s(12f), s(10f))
                        cubicTo(s(15f), s(10f), s(16f), s(8f), s(16f), s(4f))
                    }
                    val roots = Path().apply {
                        moveTo(s(6f), s(22f))
                        cubicTo(s(6f), s(16f), s(8.5f), s(12f), s(12f), s(12f))
                        cubicTo(s(15.5f), s(12f), s(18f), s(16f), s(18f), s(22f))
                    }

                    drawPath(stem, color = colors.accentOn, style = stroke)
                    drawPath(topArc, color = colors.accentOn, style = stroke)
                    drawPath(roots, color = colors.accentOn, style = stroke)
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Whole30",
                    color = colors.text,
                    fontFamily = fontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.01f * 24).sp,
                )
                Text(
                    text = "DIARY",
                    color = colors.textTertiary,
                    fontFamily = fontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (0.04f * 13).sp,
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(bottom = 14.dp)
                .size(width = 80.dp, height = 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.accent.copy(alpha = pulseAlpha)),
        )
    }
}
