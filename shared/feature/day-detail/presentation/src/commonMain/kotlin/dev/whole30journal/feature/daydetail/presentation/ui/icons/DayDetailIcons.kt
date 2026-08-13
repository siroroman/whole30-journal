package dev.whole30journal.feature.daydetail.presentation.ui.icons

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

private object DayDetailIconPaths {
    const val CHEVRON_LEFT = "m15 18-6-6 6-6"
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

private object DayDetailIcons {
    val ChevronLeft: ImageVector by lazy { outlineIcon("ChevronLeft", DayDetailIconPaths.CHEVRON_LEFT) }
}

@Composable
fun ChevronLeftIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, contentDescription: String? = null) {
    Icon(DayDetailIcons.ChevronLeft, contentDescription, modifier, tint)
}
