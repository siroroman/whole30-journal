package dev.whole30journal.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme

@Composable
fun DSConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmVariant: DSButtonVariant = DSButtonVariant.Danger,
    confirmEnabled: Boolean = true,
) {
    val colors = DSTheme.colors
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = modifier.fillMaxSize().padding(DSSpacing.space10), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .fillMaxWidth()
                    .clip(DSShapes.xxl)
                    .background(colors.surface)
                    .padding(horizontal = DSSpacing.space9, vertical = DSSpacing.space10),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DSSpacing.space6),
            ) {
                Text(
                    text = title,
                    style = DSTheme.typography.textXl,
                    color = colors.text,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = message,
                    style = DSTheme.typography.textBase,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
                DSButton(
                    onClick = onConfirm,
                    variant = confirmVariant,
                    fullWidth = true,
                    enabled = confirmEnabled,
                ) {
                    Text(confirmText)
                }
                Text(
                    text = cancelText,
                    style = DSTheme.typography.textMd,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(DSShapes.md)
                        .clickable(onClick = onDismiss)
                        .padding(DSSpacing.space6),
                )
            }
        }
    }
}

@Preview
@Composable
private fun DSConfirmDialogPreviewLight() {
    DSTheme(darkTheme = false) {
        DSConfirmDialog(
            title = "Remove this meal?",
            message = "It'll be gone once you save this day.",
            confirmText = "Remove Meal",
            cancelText = "Cancel",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun DSConfirmDialogPreviewDark() {
    DSTheme(darkTheme = true) {
        DSConfirmDialog(
            title = "Remove this meal?",
            message = "It'll be gone once you save this day.",
            confirmText = "Remove Meal",
            cancelText = "Cancel",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
