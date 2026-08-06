package dev.whole30journal.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme

@Composable
fun DSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    containerColor: Color = DSTheme.colors.bg,
    shape: Shape = DSShapes.md,
) {
    val colors = DSTheme.colors
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, colors.divider), shape),
        placeholder = if (placeholder != null) {
            { Text(text = placeholder, style = DSTheme.typography.textBase, color = colors.textTertiary) }
        } else {
            null
        },
        textStyle = DSTheme.typography.textBase,
        singleLine = singleLine,
        minLines = minLines,
        shape = shape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = colors.text,
            unfocusedTextColor = colors.text,
            cursorColor = colors.accent,
        ),
    )
}

@Preview
@Composable
private fun DSTextFieldPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            Column(
                modifier = Modifier.padding(DSSpacing.space7),
                verticalArrangement = Arrangement.spacedBy(DSSpacing.space5),
            ) {
                DSTextField(value = "", onValueChange = {}, placeholder = "Note")
                DSTextField(
                    value = "Felt strong today, energy holding steady.",
                    onValueChange = {},
                    placeholder = "What are you thinking about today?",
                    singleLine = false,
                    minLines = 3,
                    containerColor = DSTheme.colors.surface,
                )
            }
        }
    }
}

@Preview
@Composable
private fun DSTextFieldPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            Column(
                modifier = Modifier.padding(DSSpacing.space7),
                verticalArrangement = Arrangement.spacedBy(DSSpacing.space5),
            ) {
                DSTextField(value = "", onValueChange = {}, placeholder = "Note")
                DSTextField(
                    value = "Felt strong today, energy holding steady.",
                    onValueChange = {},
                    placeholder = "What are you thinking about today?",
                    singleLine = false,
                    minLines = 3,
                    containerColor = DSTheme.colors.surface,
                )
            }
        }
    }
}
