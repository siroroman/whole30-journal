package dev.whole30journal.feature.dayentry.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.whole30journal.core.designsystem.components.DSCard
import dev.whole30journal.core.designsystem.components.DSTextField
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.dayentry.presentation.generated.resources.Res
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_add_meal_button
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_cancel_button
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meal_add_photo_content_description
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meal_description_placeholder
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meal_loved_content_description
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meal_photo_content_description
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meals_title
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_photo_source_camera
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_photo_source_library
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_photo_source_title
import dev.whole30journal.feature.dayentry.presentation.photo.rememberMealPhotoPicker
import dev.whole30journal.feature.dayentry.presentation.ui.icons.HeartIcon
import dev.whole30journal.feature.dayentry.presentation.ui.icons.PlusIcon
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryContract
import org.jetbrains.compose.resources.stringResource

private val PhotoSlotSize = 60.dp

@Composable
fun MealsSection(
    meals: List<DayEntryContract.MealEntry>,
    pendingPhotoMealId: String?,
    onDescriptionChange: (id: String, description: String) -> Unit,
    onLovedToggle: (id: String) -> Unit,
    onAddPhotoClick: (id: String) -> Unit,
    onPhotoPicked: (id: String, token: String) -> Unit,
    onPhotoSourceDismiss: () -> Unit,
    onAddMealClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var awaitingPhotoMealId by remember { mutableStateOf<String?>(null) }
    val photoPicker = rememberMealPhotoPicker { token ->
        awaitingPhotoMealId?.let { onPhotoPicked(it, token) }
        awaitingPhotoMealId = null
    }

    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
        Text(text = stringResource(Res.string.day_entry_meals_title), style = DSTheme.typography.textLg, color = colors.text)
        meals.forEach { meal ->
            MealRow(
                meal = meal,
                onDescriptionChange = onDescriptionChange,
                onLovedToggle = onLovedToggle,
                onAddPhotoClick = onAddPhotoClick,
            )
        }
        AddEntryButton(text = stringResource(Res.string.day_entry_add_meal_button), onClick = onAddMealClick)
    }

    if (pendingPhotoMealId != null) {
        PhotoSourceDialog(
            onCameraClick = {
                awaitingPhotoMealId = pendingPhotoMealId
                onPhotoSourceDismiss()
                photoPicker.launchCamera()
            },
            onLibraryClick = {
                awaitingPhotoMealId = pendingPhotoMealId
                onPhotoSourceDismiss()
                photoPicker.launchLibrary()
            },
            onDismiss = onPhotoSourceDismiss,
        )
    }
}

@Composable
private fun PhotoSourceDialog(onCameraClick: () -> Unit, onLibraryClick: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.day_entry_photo_source_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DSSpacing.space3)) {
                Text(
                    text = stringResource(Res.string.day_entry_photo_source_camera),
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onCameraClick),
                )
                Text(
                    text = stringResource(Res.string.day_entry_photo_source_library),
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onLibraryClick),
                )
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = stringResource(Res.string.day_entry_cancel_button)) } },
    )
}

@Composable
private fun MealRow(
    meal: DayEntryContract.MealEntry,
    onDescriptionChange: (id: String, description: String) -> Unit,
    onLovedToggle: (id: String) -> Unit,
    onAddPhotoClick: (id: String) -> Unit,
) {
    val colors = DSTheme.colors
    DSCard(modifier = Modifier.fillMaxWidth(), contentPadding = DSSpacing.space6) {
        Row(horizontalArrangement = Arrangement.spacedBy(DSSpacing.space5), verticalAlignment = Alignment.CenterVertically) {
            if (meal.photoToken != null) {
                AsyncImage(
                    model = meal.photoToken,
                    contentDescription = stringResource(Res.string.day_entry_meal_photo_content_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(PhotoSlotSize)
                        .clip(DSShapes.md)
                        .background(colors.surface2)
                        .clickable { onAddPhotoClick(meal.id) },
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(PhotoSlotSize)
                        .dashedBorder(colors.divider, DSShapes.md)
                        .clickable { onAddPhotoClick(meal.id) },
                    contentAlignment = Alignment.Center,
                ) {
                    PlusIcon(
                        tint = colors.textTertiary,
                        modifier = Modifier.size(18.dp),
                        contentDescription = stringResource(Res.string.day_entry_meal_add_photo_content_description),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(DSSpacing.space2)) {
                Text(text = meal.label.uppercase(), style = DSTheme.typography.text2xs, color = colors.textTertiary)
                DSTextField(
                    value = meal.description,
                    onValueChange = { onDescriptionChange(meal.id, it) },
                    placeholder = stringResource(Res.string.day_entry_meal_description_placeholder),
                )
            }
            Box(
                modifier = Modifier.clickable { onLovedToggle(meal.id) },
                contentAlignment = Alignment.Center,
            ) {
                HeartIcon(
                    filled = meal.lovedIt,
                    tint = if (meal.lovedIt) colors.scoreLow else colors.textTertiary,
                    modifier = Modifier.size(19.dp),
                    contentDescription = stringResource(Res.string.day_entry_meal_loved_content_description),
                )
            }
        }
    }
}

@Preview
@Composable
private fun MealsSectionPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            MealsSection(
                meals = listOf(
                    DayEntryContract.MealEntry(id = "1", label = "Meal 1", description = "Scrambled eggs, spinach, avocado"),
                    DayEntryContract.MealEntry(id = "2", label = "Meal 2", description = "", lovedIt = true),
                ),
                pendingPhotoMealId = null,
                onDescriptionChange = { _, _ -> },
                onLovedToggle = {},
                onAddPhotoClick = {},
                onPhotoPicked = { _, _ -> },
                onPhotoSourceDismiss = {},
                onAddMealClick = {},
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}

@Preview
@Composable
private fun MealsSectionPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            MealsSection(
                meals = listOf(DayEntryContract.MealEntry(id = "1", label = "Meal 1")),
                pendingPhotoMealId = null,
                onDescriptionChange = { _, _ -> },
                onLovedToggle = {},
                onAddPhotoClick = {},
                onPhotoPicked = { _, _ -> },
                onPhotoSourceDismiss = {},
                onAddMealClick = {},
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}
