package dev.whole30journal.feature.home.presentation.ui.icons

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

/**
 * Lucide-equivalent outline icons (24x24 viewport, ~2px stroke) reproduced from the design import's
 * source SVG `d` paths - no icon library is installed in this repo, so these are hand-built
 * ImageVectors instead of a new third-party dependency. Swappable later: only the bodies here
 * change, every call site (EnergyIcon(...), etc.) stays put.
 */
private object HomeIconPaths {
    const val LEAF = "M12 2v6 M8 4c0 4 1 6 4 6s4-2 4-6 M6 22c0-6 2.5-10 6-10s6 4 6 10"
    const val BOLT = "M13 2 3 14h9l-1 8 10-12h-9z"
    const val SMILEY = "M3,12 a9,9 0 1,0 18,0 a9,9 0 1,0 -18,0 M8 14s1.5 2 4 2 4-2 4-2 M9 9h.01 M15 9h.01"
    const val MOON = "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79Z"
    const val CIRCLE_CHECK = "M3,12 a9,9 0 1,0 18,0 a9,9 0 1,0 -18,0 M9 12l2 2 4-4"
    const val PENCIL = "M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"
    const val CHEVRON_RIGHT = "m9 18 6-6-6-6"
    const val GEAR = "M4.8,12 a7.2,7.2 0 1,0 14.4,0 a7.2,7.2 0 1,0 -14.4,0 " +
        "M9,12 a3,3 0 1,0 6,0 a3,3 0 1,0 -6,0 " +
        "M19.2,12 L21.7,12 " +
        "M17.09,17.09 L18.86,18.86 " +
        "M12,19.2 L12,21.7 " +
        "M6.91,17.09 L5.14,18.86 " +
        "M4.8,12 L2.3,12 " +
        "M6.91,6.91 L5.14,5.14 " +
        "M12,4.8 L12,2.3 " +
        "M17.09,6.91 L18.86,5.14"
}

private fun outlineIcon(name: String, pathData: String, strokeWidth: Float): ImageVector =
    ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .addPath(
            pathData = addPathNodes(pathData),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
        .build()

private object HomeIcons {
    val Leaf: ImageVector by lazy { outlineIcon("Leaf", HomeIconPaths.LEAF, 2.2f) }
    val Bolt: ImageVector by lazy { outlineIcon("Bolt", HomeIconPaths.BOLT, 2f) }
    val Smiley: ImageVector by lazy { outlineIcon("Smiley", HomeIconPaths.SMILEY, 2f) }
    val Moon: ImageVector by lazy { outlineIcon("Moon", HomeIconPaths.MOON, 2f) }
    val CircleCheck: ImageVector by lazy { outlineIcon("CircleCheck", HomeIconPaths.CIRCLE_CHECK, 2f) }
    val Pencil: ImageVector by lazy { outlineIcon("Pencil", HomeIconPaths.PENCIL, 2.2f) }
    val ChevronRight: ImageVector by lazy { outlineIcon("ChevronRight", HomeIconPaths.CHEVRON_RIGHT, 2.2f) }
    val Gear: ImageVector by lazy { outlineIcon("Gear", HomeIconPaths.GEAR, 1.8f) }
}

@Composable
fun LeafIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(HomeIcons.Leaf, contentDescription, modifier, tint)
}

@Composable
fun EnergyIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(HomeIcons.Bolt, contentDescription, modifier, tint)
}

@Composable
fun MoodIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(HomeIcons.Smiley, contentDescription, modifier, tint)
}

@Composable
fun SleepIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(HomeIcons.Moon, contentDescription, modifier, tint)
}

@Composable
fun CravingsIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(HomeIcons.CircleCheck, contentDescription, modifier, tint)
}

@Composable
fun EditIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(HomeIcons.Pencil, contentDescription, modifier, tint)
}

@Composable
fun ViewDetailsIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(HomeIcons.ChevronRight, contentDescription, modifier, tint)
}

@Composable
fun SettingsIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(HomeIcons.Gear, contentDescription, modifier, tint)
}
