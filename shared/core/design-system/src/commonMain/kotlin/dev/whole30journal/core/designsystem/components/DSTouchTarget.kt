package dev.whole30journal.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import dev.whole30journal.core.designsystem.theme.DSSpacing

@Composable
fun DSTouchTarget(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = DSSpacing.space48, minHeight = DSSpacing.space48)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = contentAlignment,
        content = content,
    )
}
