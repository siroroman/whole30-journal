package dev.whole30journal.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Whole30Radii {
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 14.dp
    val xl: Dp = 16.dp
    val xxl: Dp = 20.dp
    val pill: Dp = 999.dp
}

object Whole30Shapes {
    val sm: Shape = RoundedCornerShape(Whole30Radii.sm)
    val md: Shape = RoundedCornerShape(Whole30Radii.md)
    val lg: Shape = RoundedCornerShape(Whole30Radii.lg)
    val xl: Shape = RoundedCornerShape(Whole30Radii.xl)
    val xxl: Shape = RoundedCornerShape(Whole30Radii.xxl)
    val pill: Shape = RoundedCornerShape(Whole30Radii.pill)
}
