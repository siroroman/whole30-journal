package dev.whole30journal.feature.dayentry.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import dev.whole30journal.core.designsystem.components.DSCard
import dev.whole30journal.core.designsystem.components.DSConfirmDialog
import dev.whole30journal.core.designsystem.components.DSTextField
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.dayentry.presentation.generated.resources.Res
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_add_meal_button
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_cancel_button
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_delete_meal_confirm_button
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_delete_meal_content_description
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_delete_meal_dialog_message
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_delete_meal_dialog_title
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meal_add_photo_content_description
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meal_description_placeholder
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meal_label_numbered
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meal_loved_content_description
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meal_photo_content_description
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meal_reorder_content_description
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_meals_title
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_photo_source_camera
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_photo_source_library
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_photo_source_remove
import dev.whole30journal.feature.dayentry.presentation.generated.resources.day_entry_photo_source_title
import dev.whole30journal.feature.dayentry.presentation.photo.rememberMealPhotoPicker
import dev.whole30journal.feature.dayentry.presentation.photo.rememberMealPhotoResolver
import dev.whole30journal.feature.dayentry.presentation.ui.icons.CameraIcon
import dev.whole30journal.feature.dayentry.presentation.ui.icons.CloseIcon
import dev.whole30journal.feature.dayentry.presentation.ui.icons.HeartIcon
import dev.whole30journal.feature.dayentry.presentation.ui.icons.LibraryIcon
import dev.whole30journal.feature.dayentry.presentation.ui.icons.PlusIcon
import dev.whole30journal.feature.dayentry.presentation.vm.DayEntryContract
import org.jetbrains.compose.resources.stringResource

private val PhotoSlotSize = 60.dp
private val MealActionIconSize = 24.dp

@Composable
fun MealsSection(
    meals: List<DayEntryContract.MealEntry>,
    pendingPhotoMealId: String?,
    pendingDeleteMealId: String?,
    onDescriptionChange: (id: String, description: String) -> Unit,
    onLovedToggle: (id: String) -> Unit,
    onAddPhotoClick: (id: String) -> Unit,
    onPhotoPick: (id: String, token: String) -> Unit,
    onPhotoRemove: (id: String) -> Unit,
    onPhotoSourceDismiss: () -> Unit,
    onAddMealClick: () -> Unit,
    onDeleteMealClick: (id: String) -> Unit,
    onDeleteMealConfirm: () -> Unit,
    onDeleteMealDismiss: () -> Unit,
    onReorderMeal: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var awaitingPhotoMealId by rememberSaveable { mutableStateOf<String?>(null) }
    val photoPicker = rememberMealPhotoPicker { token ->
        awaitingPhotoMealId?.let { onPhotoPick(it, token) }
        awaitingPhotoMealId = null
    }
    val resolvePhotoToken = rememberMealPhotoResolver()

    val reorderState = rememberReorderState(ids = meals.map { it.id }, onMove = onReorderMeal)

    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(EntryListSpacing)) {
        Text(text = stringResource(Res.string.day_entry_meals_title), style = DSTheme.typography.textXl, color = colors.text)
        meals.forEachIndexed { index, meal ->
            key(meal.id) {
                MealRow(
                    meal = meal,
                    number = index + 1,
                    resolvePhotoToken = resolvePhotoToken,
                    onDescriptionChange = onDescriptionChange,
                    onLovedToggle = onLovedToggle,
                    onAddPhotoClick = onAddPhotoClick,
                    onDeleteClick = onDeleteMealClick,
                    dragHandle = if (meals.size > 1) {
                        {
                            ReorderHandle(
                                state = reorderState,
                                id = meal.id,
                                contentDescription = stringResource(Res.string.day_entry_meal_reorder_content_description),
                                iconSize = MealActionIconSize,
                            )
                        }
                    } else {
                        null
                    },
                    modifier = Modifier.reorderableItem(reorderState, meal.id),
                )
            }
        }
        AddEntryButton(text = stringResource(Res.string.day_entry_add_meal_button), onClick = onAddMealClick)
    }

    if (pendingPhotoMealId != null) {
        val hasPhoto = meals.any { it.id == pendingPhotoMealId && it.photoToken != null }
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
            onRemoveClick = if (hasPhoto) {
                { onPhotoRemove(pendingPhotoMealId) }
            } else {
                null
            },
            onDismiss = onPhotoSourceDismiss,
        )
    }

    if (pendingDeleteMealId != null) {
        DSConfirmDialog(
            title = stringResource(Res.string.day_entry_delete_meal_dialog_title),
            message = stringResource(Res.string.day_entry_delete_meal_dialog_message),
            confirmText = stringResource(Res.string.day_entry_delete_meal_confirm_button),
            cancelText = stringResource(Res.string.day_entry_cancel_button),
            onConfirm = onDeleteMealConfirm,
            onDismiss = onDeleteMealDismiss,
        )
    }
}

@Composable
private fun PhotoSourceDialog(
    onCameraClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onRemoveClick: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val colors = DSTheme.colors
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().padding(DSSpacing.space10), contentAlignment = Alignment.Center) {
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
                    text = stringResource(Res.string.day_entry_photo_source_title),
                    style = DSTheme.typography.textXl,
                    color = colors.text,
                    modifier = Modifier.padding(bottom = DSSpacing.space3),
                )
                PhotoSourceOption(
                    text = stringResource(Res.string.day_entry_photo_source_camera),
                    icon = { CameraIcon(tint = colors.text, modifier = Modifier.size(18.dp)) },
                    onClick = onCameraClick,
                )
                PhotoSourceOption(
                    text = stringResource(Res.string.day_entry_photo_source_library),
                    icon = { LibraryIcon(tint = colors.text, modifier = Modifier.size(18.dp)) },
                    onClick = onLibraryClick,
                )
                if (onRemoveClick != null) {
                    PhotoSourceOption(
                        text = stringResource(Res.string.day_entry_photo_source_remove),
                        icon = { CloseIcon(tint = colors.text, modifier = Modifier.size(18.dp)) },
                        onClick = onRemoveClick,
                    )
                }
                Text(
                    text = stringResource(Res.string.day_entry_cancel_button),
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

@Composable
private fun PhotoSourceOption(text: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    val colors = DSTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DSShapes.md)
            .background(colors.surface2)
            .clickable(onClick = onClick)
            .padding(DSSpacing.space6),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Text(text = text, style = DSTheme.typography.textMd, color = colors.text, modifier = Modifier.padding(start = DSSpacing.space4))
    }
}

@Composable
private fun MealRow(
    meal: DayEntryContract.MealEntry,
    number: Int,
    resolvePhotoToken: (String) -> String,
    onDescriptionChange: (id: String, description: String) -> Unit,
    onLovedToggle: (id: String) -> Unit,
    onAddPhotoClick: (id: String) -> Unit,
    onDeleteClick: (id: String) -> Unit,
    dragHandle: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = DSTheme.colors
    DSCard(modifier = modifier.fillMaxWidth(), contentPadding = DSSpacing.space6) {
        Row(horizontalArrangement = Arrangement.spacedBy(DSSpacing.space5), verticalAlignment = Alignment.CenterVertically) {
            if (meal.photoToken != null) {
                AsyncImage(
                    model = resolvePhotoToken(meal.photoToken),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.day_entry_meal_label_numbered, number).uppercase(),
                        style = DSTheme.typography.textMd.copy(fontWeight = FontWeight.Bold),
                        color = colors.textTertiary,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(DSSpacing.space4), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.clickable { onLovedToggle(meal.id) },
                            contentAlignment = Alignment.Center,
                        ) {
                            HeartIcon(
                                filled = meal.lovedIt,
                                tint = if (meal.lovedIt) colors.scoreLow else colors.textTertiary,
                                modifier = Modifier.size(MealActionIconSize),
                                contentDescription = stringResource(Res.string.day_entry_meal_loved_content_description),
                            )
                        }
                        Box(
                            modifier = Modifier.clickable { onDeleteClick(meal.id) },
                            contentAlignment = Alignment.Center,
                        ) {
                            CloseIcon(
                                tint = colors.textTertiary,
                                modifier = Modifier.size(MealActionIconSize),
                                contentDescription = stringResource(Res.string.day_entry_delete_meal_content_description),
                            )
                        }
                        dragHandle?.invoke()
                    }
                }
                DSTextField(
                    value = meal.description,
                    onValueChange = { onDescriptionChange(meal.id, it) },
                    placeholder = stringResource(Res.string.day_entry_meal_description_placeholder),
                    singleLine = false,
                    minLines = 1,
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
                    DayEntryContract.MealEntry(id = "1", description = "Scrambled eggs, spinach, avocado"),
                    DayEntryContract.MealEntry(id = "2", description = "", lovedIt = true),
                ),
                pendingPhotoMealId = null,
                pendingDeleteMealId = null,
                onDescriptionChange = { _, _ -> },
                onLovedToggle = {},
                onAddPhotoClick = {},
                onPhotoPick = { _, _ -> },
                onPhotoRemove = {},
                onPhotoSourceDismiss = {},
                onAddMealClick = {},
                onDeleteMealClick = {},
                onDeleteMealConfirm = {},
                onDeleteMealDismiss = {},
                onReorderMeal = { _, _ -> },
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
                meals = listOf(DayEntryContract.MealEntry(id = "1")),
                pendingPhotoMealId = null,
                pendingDeleteMealId = null,
                onDescriptionChange = { _, _ -> },
                onLovedToggle = {},
                onAddPhotoClick = {},
                onPhotoPick = { _, _ -> },
                onPhotoRemove = {},
                onPhotoSourceDismiss = {},
                onAddMealClick = {},
                onDeleteMealClick = {},
                onDeleteMealConfirm = {},
                onDeleteMealDismiss = {},
                onReorderMeal = { _, _ -> },
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}

@Preview
@Composable
private fun PhotoSourceDialogPreviewLight() {
    DSTheme(darkTheme = false) {
        PhotoSourceDialog(onCameraClick = {}, onLibraryClick = {}, onRemoveClick = null, onDismiss = {})
    }
}

@Preview
@Composable
private fun PhotoSourceDialogPreviewDark() {
    DSTheme(darkTheme = true) {
        PhotoSourceDialog(onCameraClick = {}, onLibraryClick = {}, onRemoveClick = null, onDismiss = {})
    }
}

@Preview
@Composable
private fun PhotoSourceDialogWithRemovePreview() {
    DSTheme(darkTheme = false) {
        PhotoSourceDialog(onCameraClick = {}, onLibraryClick = {}, onRemoveClick = {}, onDismiss = {})
    }
}
