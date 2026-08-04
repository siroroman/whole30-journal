package dev.whole30journal.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

@Composable
fun Whole30Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = remember(darkTheme) { if (darkTheme) whole30DarkColors() else whole30LightColors() }
    val typography = remember { whole30Typography() }
    CompositionLocalProvider(
        LocalWhole30Colors provides colors,
        LocalWhole30Typography provides typography,
        content = content,
    )
}

object Whole30Theme {
    val colors: Whole30Colors
        @Composable
        @ReadOnlyComposable
        get() = LocalWhole30Colors.current

    val typography: Whole30Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalWhole30Typography.current
}
