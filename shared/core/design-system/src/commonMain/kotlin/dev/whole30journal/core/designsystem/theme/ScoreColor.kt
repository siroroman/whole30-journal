package dev.whole30journal.core.designsystem.theme

import androidx.compose.ui.graphics.Color

fun DSColors.scoreColor(score: Int?): Color = when {
    score == null -> track
    score <= 4 -> scoreLow
    score <= 7 -> scoreMid
    else -> scoreHigh
}
