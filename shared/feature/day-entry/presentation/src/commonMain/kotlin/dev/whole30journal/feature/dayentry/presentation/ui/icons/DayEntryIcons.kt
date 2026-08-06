package dev.whole30journal.feature.dayentry.presentation.ui.icons

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private object DayEntryIconPaths {
    const val HEART = "M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2" +
        "-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"
    const val PLUS = "M5 12h14 M12 5v14"
}

private fun heartVector(name: String, filled: Boolean): ImageVector =
    ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .addPath(
            pathData = addPathNodes(DayEntryIconPaths.HEART),
            fill = if (filled) SolidColor(Color.Black) else null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
        .build()

private object DayEntryIcons {
    val HeartOutline: ImageVector by lazy { heartVector("HeartOutline", filled = false) }
    val HeartFilled: ImageVector by lazy { heartVector("HeartFilled", filled = true) }
    val Plus: ImageVector by lazy {
        ImageVector.Builder(name = "Plus", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .addPath(
                pathData = addPathNodes(DayEntryIconPaths.PLUS),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            .build()
    }
}

@Composable
fun HeartIcon(filled: Boolean, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(if (filled) DayEntryIcons.HeartFilled else DayEntryIcons.HeartOutline, contentDescription, modifier, tint)
}

@Composable
fun PlusIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(DayEntryIcons.Plus, contentDescription, modifier, tint)
}
