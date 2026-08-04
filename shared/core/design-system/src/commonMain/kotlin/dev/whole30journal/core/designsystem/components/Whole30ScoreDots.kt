package dev.whole30journal.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.theme.Whole30Shapes
import dev.whole30journal.core.designsystem.theme.Whole30Theme
import dev.whole30journal.core.designsystem.theme.scoreColor

private val DotGap = 5.dp

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
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f),
    ) {
        val cellWidth = maxWidth / max
        val dotSize = cellWidth - DotGap
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dotValue in 1..max) {
                val filled = score != null && dotValue <= score
                Box(
                    modifier = Modifier
                        .width(cellWidth)
                        .heightIn(min = 48.dp)
                        .then(
                            if (enabled && onScoreChange != null) {
                                Modifier.clickable(
                                    onClickLabel = "Set to $dotValue",
                                    role = Role.Button,
                                ) { onScoreChange(dotValue) }
                            } else {
                                Modifier
                            },
                        )
                        .semantics {
                            contentDescription = "$dotValue of $max"
                            stateDescription = if (filled) "Filled" else "Empty"
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .clip(Whole30Shapes.pill)
                            .background(if (filled) filledColor else colors.track),
                    )
                }
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
