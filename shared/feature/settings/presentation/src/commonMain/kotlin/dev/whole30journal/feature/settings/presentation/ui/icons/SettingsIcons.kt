package dev.whole30journal.feature.settings.presentation.ui.icons

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

private object SettingsIconPaths {
    const val CHEVRON_LEFT = "m15 18-6-6 6-6"
    const val MINUS = "M5 12h14"
    const val PLUS = "M5 12h14 M12 5v14"
}

private fun outlineIcon(name: String, pathData: String, strokeWidth: Float = 2.2f): ImageVector =
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

private object SettingsIcons {
    val ChevronLeft: ImageVector by lazy { outlineIcon("ChevronLeft", SettingsIconPaths.CHEVRON_LEFT) }
    val Minus: ImageVector by lazy { outlineIcon("Minus", SettingsIconPaths.MINUS) }
    val Plus: ImageVector by lazy { outlineIcon("Plus", SettingsIconPaths.PLUS) }
}

@Composable
fun ChevronLeftIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(SettingsIcons.ChevronLeft, contentDescription, modifier, tint)
}

@Composable
fun MinusIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(SettingsIcons.Minus, contentDescription, modifier, tint)
}

@Composable
fun PlusIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(SettingsIcons.Plus, contentDescription, modifier, tint)
}
