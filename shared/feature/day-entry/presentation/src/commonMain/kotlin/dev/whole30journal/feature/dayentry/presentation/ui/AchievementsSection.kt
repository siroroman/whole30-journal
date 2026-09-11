package dev.whole30journal.feature.dayentry.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.components.DSCard
import dev.whole30journal.core.designsystem.components.DSConfirmDialog
import dev.whole30journal.core.designsystem.components.DSTextField
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.dayentry.presentation.generated.resources.Res
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_add_achievement_button
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_cancel_button
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_delete_achievement_confirm_button
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_delete_achievement_content_description
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_delete_achievement_dialog_message
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_delete_achievement_dialog_title
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_nsv_placeholder
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_nsv_title
import dev.whole30journal.feature.dayentry.presentation.ui.icons.CloseIcon
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun AchievementsSection(
    achievements: List<DayEntryContract.AchievementEntry>,
    pendingDeleteAchievementId: String?,
    onTextChange: (id: String, text: String) -> Unit,
    onAddClick: () -> Unit,
    onDeleteAchievementClick: (id: String) -> Unit,
    onDeleteAchievementConfirm: () -> Unit,
    onDeleteAchievementDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
        Text(text = stringResource(Res.string.day_entry_nsv_title), style = DSTheme.typography.textLg, color = colors.text)
        achievements.forEach { entry ->
            key(entry.id) {
                AchievementRow(
                    achievement = entry,
                    onTextChange = onTextChange,
                    onDeleteClick = onDeleteAchievementClick,
                )
            }
        }
        AddEntryButton(text = stringResource(Res.string.day_entry_add_achievement_button), onClick = onAddClick)
    }

    if (pendingDeleteAchievementId != null) {
        DSConfirmDialog(
            title = stringResource(Res.string.day_entry_delete_achievement_dialog_title),
            message = stringResource(Res.string.day_entry_delete_achievement_dialog_message),
            confirmText = stringResource(Res.string.day_entry_delete_achievement_confirm_button),
            cancelText = stringResource(Res.string.day_entry_cancel_button),
            onConfirm = onDeleteAchievementConfirm,
            onDismiss = onDeleteAchievementDismiss,
        )
    }
}

@Composable
private fun AchievementRow(
    achievement: DayEntryContract.AchievementEntry,
    onTextChange: (id: String, text: String) -> Unit,
    onDeleteClick: (id: String) -> Unit,
) {
    val colors = DSTheme.colors
    DSCard(modifier = Modifier.fillMaxWidth(), contentPadding = DSSpacing.space6) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSSpacing.space3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DSTextField(
                value = achievement.text,
                onValueChange = { onTextChange(achievement.id, it) },
                placeholder = stringResource(Res.string.day_entry_nsv_placeholder),
                containerColor = colors.surface,
                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
                singleLine = false,
                minLines = 2,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier.clickable { onDeleteClick(achievement.id) },
                contentAlignment = Alignment.Center,
            ) {
                CloseIcon(
                    tint = colors.textTertiary,
                    modifier = Modifier.size(16.dp),
                    contentDescription = stringResource(Res.string.day_entry_delete_achievement_content_description),
                )
            }
        }
    }
}

@Preview
@Composable
private fun AchievementsSectionPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            AchievementsSection(
                achievements = listOf(
                    DayEntryContract.AchievementEntry(id = "1", text = "Cooked a full dinner from scratch"),
                ),
                pendingDeleteAchievementId = null,
                onTextChange = { _, _ -> },
                onAddClick = {},
                onDeleteAchievementClick = {},
                onDeleteAchievementConfirm = {},
                onDeleteAchievementDismiss = {},
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}

@Preview
@Composable
private fun AchievementsSectionPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            AchievementsSection(
                achievements = listOf(
                    DayEntryContract.AchievementEntry(id = "1", text = ""),
                ),
                pendingDeleteAchievementId = null,
                onTextChange = { _, _ -> },
                onAddClick = {},
                onDeleteAchievementClick = {},
                onDeleteAchievementConfirm = {},
                onDeleteAchievementDismiss = {},
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}
