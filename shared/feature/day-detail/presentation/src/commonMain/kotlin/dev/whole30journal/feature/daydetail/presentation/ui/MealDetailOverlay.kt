package dev.whole30journal.feature.daydetail.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.daydetail.presentation.generated.resources.Res
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_meal_detail_close_content_description
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_meal_photo_content_description
import dev.whole30journal.feature.daydetail.presentation.vm.DayDetailContract
import dev.whole30journal.feature.dayentry.presentation.photo.rememberMealPhotoResolver
import dev.whole30journal.feature.dayentry.presentation.ui.icons.CloseIcon
import dev.whole30journal.feature.dayentry.presentation.ui.icons.LibraryIcon
import org.jetbrains.compose.resources.stringResource

private val PlaceholderIconSize = 96.dp
private val DescriptionMaxHeight = 160.dp
private val CloseButtonClearance = 56.dp

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
private fun MealDetailContent(meal: DayDetailContract.MealSummary, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    val resolvePhotoToken = rememberMealPhotoResolver()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .clickable(interactionSource = null, indication = null, onClick = onDismiss)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = CloseButtonClearance),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DSSpacing.space10),
        ) {
            if (meal.photoToken != null) {
                AsyncImage(
                    model = resolvePhotoToken(meal.photoToken),
                    contentDescription = stringResource(Res.string.day_detail_meal_photo_content_description),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = true),
                )
            } else {
                LibraryIcon(tint = colors.textTertiary, modifier = Modifier.size(PlaceholderIconSize))
            }
            Text(
                text = meal.description.ifBlank { meal.label },
                style = DSTheme.typography.text2xl,
                color = colors.text,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = DescriptionMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = DSSpacing.space7),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(DSSpacing.space4)
                .clip(DSShapes.pill)
                .clickable(onClick = onDismiss)
                .padding(DSSpacing.space4),
        ) {
            CloseIcon(
                tint = colors.textSecondary,
                contentDescription = stringResource(Res.string.day_detail_meal_detail_close_content_description),
            )
        }
    }
}

@Preview
@Composable
private fun MealDetailOverlayPreviewLight() {
    DSTheme(darkTheme = false) {
        MealDetailOverlay(
            meal = DayDetailContract.MealSummary(
                id = "1",
                label = "Meal 1",
                description = "Grilled chicken, roasted broccoli, sweet potato",
                photoToken = null,
                lovedIt = true,
            ),
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun MealDetailOverlayPreviewDark() {
    DSTheme(darkTheme = true) {
        MealDetailOverlay(
            meal = DayDetailContract.MealSummary(id = "2", label = "Meal 2", description = "", photoToken = null, lovedIt = true),
            onDismiss = {},
        )
    }
}
