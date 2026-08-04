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
import dev.whole30journal.core.designsystem.theme.Whole30Shapes
import dev.whole30journal.core.designsystem.theme.Whole30Spacing
import dev.whole30journal.core.designsystem.theme.Whole30Theme

enum class Whole30ButtonVariant { Primary, Secondary, Ghost }
enum class Whole30ButtonSize { Small, Medium }

private const val DISABLED_ALPHA = 0.45f

/** Primary is the only filled surface in this system - use it once per screen. */
@Composable
fun Whole30Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: Whole30ButtonVariant = Whole30ButtonVariant.Primary,
    size: Whole30ButtonSize = Whole30ButtonSize.Medium,
    fullWidth: Boolean = false,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = Whole30Theme.colors
    val textStyle = (if (size == Whole30ButtonSize.Small) Whole30Theme.typography.textSm else Whole30Theme.typography.textMd)
        .copy(fontWeight = FontWeight.Bold)
    val contentPadding = if (size == Whole30ButtonSize.Small) {
        PaddingValues(horizontal = Whole30Spacing.space7, vertical = Whole30Spacing.space4)
    } else {
        PaddingValues(Whole30Spacing.space6)
    }
    val buttonModifier = if (fullWidth) modifier.fillMaxWidth() else modifier
    val label: @Composable RowScope.() -> Unit = {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(Whole30Spacing.space3))
        }
        ProvideTextStyle(textStyle) { content() }
    }

    when (variant) {
        Whole30ButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = Whole30Shapes.lg,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.accentOn,
                disabledContainerColor = colors.accent.copy(alpha = DISABLED_ALPHA),
                disabledContentColor = colors.accentOn.copy(alpha = DISABLED_ALPHA),
            ),
            content = label,
        )
        Whole30ButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = Whole30Shapes.lg,
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
        Whole30ButtonVariant.Ghost -> TextButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = Whole30Shapes.lg,
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
private fun Whole30ButtonPreviewLight() {
    Whole30Theme(darkTheme = false) {
        Surface(color = Whole30Theme.colors.bg) {
            Column(
                modifier = Modifier.padding(Whole30Spacing.space7),
                verticalArrangement = Arrangement.spacedBy(Whole30Spacing.space5),
            ) {
                Whole30Button(onClick = {}, fullWidth = true) { Text("Save Day 12") }
                Whole30Button(onClick = {}, variant = Whole30ButtonVariant.Secondary) { Text("Cancel") }
                Whole30Button(onClick = {}, variant = Whole30ButtonVariant.Ghost) { Text("Edit") }
                Whole30Button(onClick = {}, enabled = false) { Text("Disabled") }
            }
        }
    }
}

@Preview
@Composable
private fun Whole30ButtonPreviewDark() {
    Whole30Theme(darkTheme = true) {
        Surface(color = Whole30Theme.colors.bg) {
            Column(
                modifier = Modifier.padding(Whole30Spacing.space7),
                verticalArrangement = Arrangement.spacedBy(Whole30Spacing.space5),
            ) {
                Whole30Button(onClick = {}, fullWidth = true) { Text("Save Day 12") }
                Whole30Button(onClick = {}, variant = Whole30ButtonVariant.Secondary) { Text("Cancel") }
                Whole30Button(onClick = {}, variant = Whole30ButtonVariant.Ghost) { Text("Edit") }
                Whole30Button(onClick = {}, enabled = false) { Text("Disabled") }
            }
        }
    }
}
