package dev.whole30journal.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.theme.Whole30Shapes
import dev.whole30journal.core.designsystem.theme.Whole30Theme
import dev.whole30journal.core.designsystem.theme.scoreColor

private val DotGap = 5.dp

/** Tap-to-rate row of [max] dots, used on the Entry screen for Energy / Mood / Sleep / Cravings. */
@Composable
fun Whole30ScoreDots(
    score: Int?,
    modifier: Modifier = Modifier,
    max: Int = 10,
    enabled: Boolean = true,
    onScoreChange: ((Int) -> Unit)? = null,
) {
    val colors = Whole30Theme.colors
    val filledColor = colors.scoreColor(score)
    // Sized via BoxWithConstraints rather than RowScope.weight(): Modifier.weight()'s inline body
    // leaks an internal Compose Foundation property in a way Kotlin 2.4.0 now rejects at compile
    // time (a real toolchain incompatibility, not specific to this component).
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f),
    ) {
        val dotSize = (maxWidth - DotGap * (max - 1)) / max
        Row(horizontalArrangement = Arrangement.spacedBy(DotGap)) {
            for (dotValue in 1..max) {
                val filled = score != null && dotValue <= score
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(Whole30Shapes.pill)
                        .background(if (filled) filledColor else colors.track)
                        .then(
                            if (enabled && onScoreChange != null) {
                                Modifier.clickable { onScoreChange(dotValue) }
                            } else {
                                Modifier
                            },
                        ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun Whole30ScoreDotsPreviewLight() {
    Whole30Theme(darkTheme = false) {
        Surface(color = Whole30Theme.colors.bg) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Whole30ScoreDots(score = 6, onScoreChange = {})
                Whole30ScoreDots(score = 7, enabled = false)
                Whole30ScoreDots(score = null, onScoreChange = {})
            }
        }
    }
}

@Preview
@Composable
private fun Whole30ScoreDotsPreviewDark() {
    Whole30Theme(darkTheme = true) {
        Surface(color = Whole30Theme.colors.bg) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Whole30ScoreDots(score = 6, onScoreChange = {})
                Whole30ScoreDots(score = 7, enabled = false)
                Whole30ScoreDots(score = null, onScoreChange = {})
            }
        }
    }
}
