package dev.whole30journal.feature.dayentry.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.designsystem.components.DSTextField
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.dayentry.presentation.generated.resources.Res
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_nsv_add_content_description
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_nsv_placeholder
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_nsv_title
import dev.whole30journal.feature.dayentry.presentation.ui.icons.PlusIcon
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun AchievementsSection(
    achievements: List<DayEntryContract.AchievementEntry>,
    pickerOpen: Boolean,
    chipLabels: List<String>,
    onTogglePicker: () -> Unit,
    onTextChange: (id: String, text: String) -> Unit,
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(Res.string.day_entry_nsv_title), style = DSTheme.typography.textLg, color = colors.text)
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(28.dp)
                    .clip(DSShapes.pill)
                    .background(colors.surface)
                    .clickable(onClick = onTogglePicker),
                contentAlignment = Alignment.Center,
            ) {
                PlusIcon(
                    tint = colors.accent,
                    modifier = Modifier.size(14.dp),
                    contentDescription = stringResource(Res.string.day_entry_nsv_add_content_description),
                )
            }
        }
        if (pickerOpen) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(DSShapes.md)
                    .background(colors.surface)
                    .padding(DSSpacing.space4),
                horizontalArrangement = Arrangement.spacedBy(DSSpacing.space3),
                verticalArrangement = Arrangement.spacedBy(DSSpacing.space3),
            ) {
                chipLabels.forEach { label ->
                    Text(
                        text = label,
                        style = DSTheme.typography.textSm,
                        color = colors.text,
                        modifier = Modifier
                            .clip(DSShapes.pill)
                            .border(1.dp, colors.divider, DSShapes.pill)
                            .clickable { onChipClick(label) }
                            .padding(horizontal = DSSpacing.space5, vertical = DSSpacing.space3),
                    )
                }
            }
        }
        achievements.forEach { entry ->
            DSTextField(
                value = entry.text,
                onValueChange = { onTextChange(entry.id, it) },
                placeholder = stringResource(Res.string.day_entry_nsv_placeholder),
                containerColor = colors.surface,
            )
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
                    DayEntryContract.AchievementEntry(id = "2", text = ""),
                    DayEntryContract.AchievementEntry(id = "3", text = ""),
                ),
                pickerOpen = true,
                chipLabels = listOf("More patient", "Less stressed", "Clearer skin"),
                onTogglePicker = {},
                onTextChange = { _, _ -> },
                onChipClick = {},
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
                    DayEntryContract.AchievementEntry(id = "2", text = ""),
                    DayEntryContract.AchievementEntry(id = "3", text = ""),
                ),
                pickerOpen = false,
                chipLabels = emptyList(),
                onTogglePicker = {},
                onTextChange = { _, _ -> },
                onChipClick = {},
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}
