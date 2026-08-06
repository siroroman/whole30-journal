package dev.whole30journal.feature.dayentry.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.whole30journal.core.designsystem.components.DSTextField
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.dayentry.presentation.generated.resources.Res
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_add_achievement_button
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_nsv_placeholder
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_nsv_title
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun AchievementsSection(
    achievements: List<DayEntryContract.AchievementEntry>,
    onTextChange: (id: String, text: String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
        Text(text = stringResource(Res.string.day_entry_nsv_title), style = DSTheme.typography.textLg, color = colors.text)
        achievements.forEach { entry ->
            DSTextField(
                value = entry.text,
                onValueChange = { onTextChange(entry.id, it) },
                placeholder = stringResource(Res.string.day_entry_nsv_placeholder),
                containerColor = colors.surface,
                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
            )
        }
        AddEntryButton(text = stringResource(Res.string.day_entry_add_achievement_button), onClick = onAddClick)
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
                onTextChange = { _, _ -> },
                onAddClick = {},
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
                onTextChange = { _, _ -> },
                onAddClick = {},
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}
