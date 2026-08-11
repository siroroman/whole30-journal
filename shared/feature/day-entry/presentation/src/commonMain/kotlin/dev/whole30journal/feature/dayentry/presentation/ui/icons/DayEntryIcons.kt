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
    const val CAMERA_BODY = "M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z"
    const val CAMERA_LENS = "M8.5,13 a3.5,3.5 0 1,0 7,0 a3.5,3.5 0 1,0 -7,0"
    const val LIBRARY_FRAME = "M6,3 H18 A3,3 0 0 1 21,6 V18 A3,3 0 0 1 18,21 H6 A3,3 0 0 1 3,18 V6 A3,3 0 0 1 6,3 Z"
    const val LIBRARY_DOT = "M7,8.5 a1.5,1.5 0 1,0 3,0 a1.5,1.5 0 1,0 -3,0"
    const val LIBRARY_MOUNTAIN = "m21 15-5-5L5 21"
}

private fun outlineVector(name: String, vararg pathData: String, strokeLineWidth: Float = 2f): ImageVector =
    ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .apply {
            pathData.forEach { data ->
                addPath(
                    pathData = addPathNodes(data),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = strokeLineWidth,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }
        .build()

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
    val Camera: ImageVector by lazy { outlineVector("Camera", DayEntryIconPaths.CAMERA_BODY, DayEntryIconPaths.CAMERA_LENS) }
    val Library: ImageVector by lazy {
        outlineVector("Library", DayEntryIconPaths.LIBRARY_FRAME, DayEntryIconPaths.LIBRARY_DOT, DayEntryIconPaths.LIBRARY_MOUNTAIN)
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

@Composable
fun CameraIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(DayEntryIcons.Camera, contentDescription, modifier, tint)
}

@Composable
fun LibraryIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(DayEntryIcons.Library, contentDescription, modifier, tint)
}
