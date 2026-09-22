package dev.whole30journal.core.designsystem.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode

@Composable
fun DSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = remember(darkTheme) { if (darkTheme) dsDarkColors() else dsLightColors() }
    val typography = remember { dsTypography() }
    CompositionLocalProvider(
        LocalDSColor provides colors,
        LocalDSTypography provides typography,
        LocalIndication provides NoIndicationNodeFactory,
    ) {
        MaterialTheme(content = content)
    }
}

private object NoIndicationNodeFactory : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = NoIndicationNode()
    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = 0
}

private class NoIndicationNode : Modifier.Node(), DrawModifierNode {
    override fun ContentDrawScope.draw() {
        drawContent()
    }
}

object DSTheme {
    val colors: DSColor
        @Composable
        @ReadOnlyComposable
        get() = LocalDSColor.current

    val typography: DSTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalDSTypography.current
}
