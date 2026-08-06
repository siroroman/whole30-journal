package dev.whole30journal.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

@Composable
fun DSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = remember(darkTheme) { if (darkTheme) dsDarkColors() else dsLightColors() }
    val typography = remember { dsTypography() }
    CompositionLocalProvider(
        LocalDSColors provides colors,
        LocalDSTypography provides typography,
    ) {
        MaterialTheme(content = content)
    }
}

object DSTheme {
    val colors: DSColors
        @Composable
        @ReadOnlyComposable
        get() = LocalDSColors.current

    val typography: DSTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalDSTypography.current
}
