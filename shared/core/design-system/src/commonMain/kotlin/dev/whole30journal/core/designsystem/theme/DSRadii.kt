package dev.whole30journal.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object DSRadii {
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 14.dp
    val xl: Dp = 16.dp
    val xxl: Dp = 20.dp
    val pill: Dp = 999.dp
}

object DSShapes {
    val sm: Shape = RoundedCornerShape(DSRadii.sm)
    val md: Shape = RoundedCornerShape(DSRadii.md)
    val lg: Shape = RoundedCornerShape(DSRadii.lg)
    val xl: Shape = RoundedCornerShape(DSRadii.xl)
    val xxl: Shape = RoundedCornerShape(DSRadii.xxl)
    val pill: Shape = RoundedCornerShape(DSRadii.pill)
}
