package dev.whole30journal.feature.daydetail.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.daydetail.presentation.generated.resources.Res
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_meal_detail_close_content_description
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_meal_photo_content_description
import dev.whole30journal.feature.daydetail.presentation.generated.resources.meal_detail_preview_sample
import dev.whole30journal.feature.daydetail.presentation.vm.DayDetailContract
import dev.whole30journal.feature.dayentry.presentation.photo.rememberMealPhotoResolver
import dev.whole30journal.feature.dayentry.presentation.ui.icons.CloseIcon
import dev.whole30journal.feature.dayentry.presentation.ui.icons.LibraryIcon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val PlaceholderIconSize = 96.dp

@Composable
fun MealDetailOverlay(meal: DayDetailContract.MealSummary?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = meal,
        modifier = modifier.fillMaxSize(),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { target ->
        if (target != null) {
            MealDetailContent(meal = target, onDismiss = onDismiss)
        }
    }
}

@Composable
private fun MealDetailContent(
    meal: DayDetailContract.MealSummary,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    previewPhoto: Painter? = null,
) {
    val colors = DSTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .clickable(interactionSource = null, indication = null, onClick = onDismiss)
            .systemBarsPadding(),
    ) {
        Column {
            CloseIcon(
                tint = colors.text,
                contentDescription = stringResource(Res.string.day_detail_meal_detail_close_content_description),
                modifier = Modifier
                    .align(alignment = Alignment.End)
                    .padding(top = DSSpacing.space16, end = DSSpacing.space16)
            )
            Text(
                text = meal.description.ifBlank { meal.label },
                style = DSTheme.typography.textLg,
                color = colors.text,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = DSSpacing.space16),
                maxLines = 5
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(bottom = DSSpacing.space24),
            ) {
                ImageView(meal = meal, previewPhoto = previewPhoto)
            }
        }
    }
}

@Composable
private fun ImageView(
    meal: DayDetailContract.MealSummary,
    previewPhoto: Painter? = null
) {
    if (previewPhoto != null) {
        Image(
            painter = previewPhoto,
            contentDescription = stringResource(Res.string.day_detail_meal_photo_content_description),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        )
    } else if (meal.photoToken != null) {
        val resolvePhotoToken = rememberMealPhotoResolver()
        AsyncImage(
            model = resolvePhotoToken(meal.photoToken),
            contentDescription = stringResource(Res.string.day_detail_meal_photo_content_description),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        LibraryIcon(
            tint = DSTheme.colors.textTertiary,
            modifier = Modifier
                .size(PlaceholderIconSize)
        )
    }
}

@Preview
@Composable
private fun MealDetailOverlayPreviewLight() {
    DSTheme(darkTheme = false) {
        MealDetailContent(
            meal = DayDetailContract.MealSummary(
                id = "1",
                label = "Meal 1",
                description = "Grilled chicken, roasted broccoli, sweet potato",
                photoToken = null,
                lovedIt = true,
            ),
            onDismiss = {},
            previewPhoto = painterResource(Res.drawable.meal_detail_preview_sample),
        )
    }
}

@Preview
@Composable
private fun MealDetailOverlayPreviewDark() {
    DSTheme(darkTheme = true) {
        MealDetailContent(
            meal = DayDetailContract.MealSummary(id = "2", label = "Meal 2", description = "", photoToken = null, lovedIt = true),
            onDismiss = {},
            previewPhoto = painterResource(Res.drawable.meal_detail_preview_sample),
        )
    }
}

@Preview
@Composable
private fun MealDetailOverlayEmptyPreviewLight() {
    DSTheme(darkTheme = false) {
        MealDetailContent(
            meal = DayDetailContract.MealSummary(id = "2", label = "Meal 2", description = "", photoToken = null, lovedIt = true),
            onDismiss = {},
            previewPhoto = null,
        )
    }
}

@Preview
@Composable
private fun MealDetailOverlayEmptyPreviewDark() {
    DSTheme(darkTheme = true) {
        MealDetailContent(
            meal = DayDetailContract.MealSummary(id = "2", label = "Meal 2", description = "", photoToken = null, lovedIt = true),
            onDismiss = {},
            previewPhoto = null,
        )
    }
}
