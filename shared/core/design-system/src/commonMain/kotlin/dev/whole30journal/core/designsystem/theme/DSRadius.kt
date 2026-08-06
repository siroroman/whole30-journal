package dev.whole30journal.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object DSRadius {
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 14.dp
    val xl: Dp = 16.dp
    val xxl: Dp = 20.dp
    val pill: Dp = 999.dp
}

object DSShapes {
    val sm: Shape = RoundedCornerShape(DSRadius.sm)
    val md: Shape = RoundedCornerShape(DSRadius.md)
    val lg: Shape = RoundedCornerShape(DSRadius.lg)
    val xl: Shape = RoundedCornerShape(DSRadius.xl)
    val xxl: Shape = RoundedCornerShape(DSRadius.xxl)
    val pill: Shape = RoundedCornerShape(DSRadius.pill)
}
