package dev.whole30journal.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme

enum class DSButtonVariant { Primary, Secondary, Ghost }
enum class DSButtonSize { Small, Medium }

private const val DISABLED_ALPHA = 0.45f

@Composable
fun DSButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DSButtonVariant = DSButtonVariant.Primary,
    size: DSButtonSize = DSButtonSize.Medium,
    fullWidth: Boolean = false,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = DSTheme.colors
    val textStyle = (if (size == DSButtonSize.Small) DSTheme.typography.textSm else DSTheme.typography.textMd)
        .copy(fontWeight = FontWeight.Bold)
    val contentPadding = if (size == DSButtonSize.Small) {
        PaddingValues(horizontal = DSSpacing.space7, vertical = DSSpacing.space4)
    } else {
        PaddingValues(DSSpacing.space6)
    }
    val buttonModifier = if (fullWidth) modifier.fillMaxWidth() else modifier
    val label: @Composable RowScope.() -> Unit = {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(DSSpacing.space3))
        }
        ProvideTextStyle(textStyle) { content() }
    }

    when (variant) {
        DSButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = DSShapes.lg,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.accentOn,
                disabledContainerColor = colors.accent.copy(alpha = DISABLED_ALPHA),
                disabledContentColor = colors.accentOn.copy(alpha = DISABLED_ALPHA),
            ),
            content = label,
        )
        DSButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = DSShapes.lg,
            contentPadding = contentPadding,
            border = BorderStroke(1.dp, if (enabled) colors.divider else colors.divider.copy(alpha = DISABLED_ALPHA)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colors.surface,
                contentColor = colors.text,
                disabledContainerColor = colors.surface.copy(alpha = DISABLED_ALPHA),
                disabledContentColor = colors.text.copy(alpha = DISABLED_ALPHA),
            ),
            content = label,
        )
        DSButtonVariant.Ghost -> TextButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = DSShapes.lg,
            contentPadding = contentPadding,
            colors = ButtonDefaults.textButtonColors(
                contentColor = colors.accent,
                disabledContentColor = colors.accent.copy(alpha = DISABLED_ALPHA),
            ),
            content = label,
        )
    }
}

@Preview
@Composable
private fun DSButtonPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            Column(
                modifier = Modifier.padding(DSSpacing.space7),
                verticalArrangement = Arrangement.spacedBy(DSSpacing.space5),
            ) {
                DSButton(onClick = {}, fullWidth = true) { Text("Save Day 12") }
                DSButton(onClick = {}, variant = DSButtonVariant.Secondary) { Text("Cancel") }
                DSButton(onClick = {}, variant = DSButtonVariant.Ghost) { Text("Edit") }
                DSButton(onClick = {}, enabled = false) { Text("Disabled") }
            }
        }
    }
}

@Preview
@Composable
private fun DSButtonPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            Column(
                modifier = Modifier.padding(DSSpacing.space7),
                verticalArrangement = Arrangement.spacedBy(DSSpacing.space5),
            ) {
                DSButton(onClick = {}, fullWidth = true) { Text("Save Day 12") }
                DSButton(onClick = {}, variant = DSButtonVariant.Secondary) { Text("Cancel") }
                DSButton(onClick = {}, variant = DSButtonVariant.Ghost) { Text("Edit") }
                DSButton(onClick = {}, enabled = false) { Text("Disabled") }
            }
        }
    }
}
