package dev.whole30journal.feature.daydetail.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.whole30journal.core.designsystem.components.DSCard
import dev.whole30journal.core.designsystem.theme.DSShapes
import dev.whole30journal.core.designsystem.theme.DSSpacing
import dev.whole30journal.core.designsystem.theme.DSTheme
import dev.whole30journal.feature.daydetail.presentation.generated.resources.Res
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_meal_loved_content_description
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_meal_not_loved_content_description
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_meal_photo_content_description
import dev.whole30journal.feature.daydetail.presentation.generated.resources.day_detail_meals_title
import dev.whole30journal.feature.daydetail.presentation.vm.DayDetailContract
import dev.whole30journal.feature.dayentry.presentation.photo.rememberMealPhotoResolver
import dev.whole30journal.feature.dayentry.presentation.ui.icons.HeartIcon
import org.jetbrains.compose.resources.stringResource

private val PhotoSlotSize = 52.dp

@Composable
fun MealsSummarySection(meals: List<DayDetailContract.MealSummary>, modifier: Modifier = Modifier) {
    if (meals.isEmpty()) return
    val resolvePhotoToken = rememberMealPhotoResolver()
    val colors = DSTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(DSSpacing.space5)) {
        Text(text = stringResource(Res.string.day_detail_meals_title), style = DSTheme.typography.textLg, color = colors.text)
        meals.forEach { meal -> MealSummaryRow(meal = meal, resolvePhotoToken = resolvePhotoToken) }
    }
}

@Composable
private fun MealSummaryRow(meal: DayDetailContract.MealSummary, resolvePhotoToken: (String) -> String, modifier: Modifier = Modifier) {
    val colors = DSTheme.colors
    DSCard(modifier = modifier.fillMaxWidth(), contentPadding = DSSpacing.space6) {
        Row(horizontalArrangement = Arrangement.spacedBy(DSSpacing.space5), verticalAlignment = Alignment.CenterVertically) {
            if (meal.photoToken != null) {
                AsyncImage(
                    model = resolvePhotoToken(meal.photoToken),
                    contentDescription = stringResource(Res.string.day_detail_meal_photo_content_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(PhotoSlotSize).clip(DSShapes.md).background(colors.surface2),
                )
            } else {
                Box(modifier = Modifier.size(PhotoSlotSize).dashedBorder(colors.divider, DSShapes.md))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(DSSpacing.space2)) {
                Text(text = meal.label.uppercase(), style = DSTheme.typography.text2xs, color = colors.textTertiary)
                Text(text = meal.description, style = DSTheme.typography.textSm, color = colors.text)
            }
            HeartIcon(
                filled = meal.lovedIt,
                tint = if (meal.lovedIt) colors.scoreLow else colors.textTertiary,
                contentDescription = if (meal.lovedIt) {
                    stringResource(Res.string.day_detail_meal_loved_content_description)
                } else {
                    stringResource(Res.string.day_detail_meal_not_loved_content_description)
                },
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private fun Modifier.dashedBorder(color: Color, shape: Shape, width: Dp = 1.5.dp): Modifier = drawWithContent {
    drawContent()
    drawOutline(
        outline = shape.createOutline(size, layoutDirection, this),
        color = color,
        style = Stroke(width = width.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))),
    )
}

@Preview
@Composable
private fun MealsSummarySectionPreviewLight() {
    DSTheme(darkTheme = false) {
        Surface(color = DSTheme.colors.bg) {
            MealsSummarySection(
                meals = listOf(
                    DayDetailContract.MealSummary(
                        id = "1",
                        label = "Meal 1",
                        description = "Scrambled eggs, spinach, avocado",
                        photoToken = null,
                        lovedIt = false,
                    ),
                    DayDetailContract.MealSummary(
                        id = "2",
                        label = "Meal 2",
                        description = "Grilled chicken, roasted broccoli, sweet potato",
                        photoToken = null,
                        lovedIt = true,
                    ),
                ),
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}

@Preview
@Composable
private fun MealsSummarySectionPreviewDark() {
    DSTheme(darkTheme = true) {
        Surface(color = DSTheme.colors.bg) {
            MealsSummarySection(
                meals = listOf(
                    DayDetailContract.MealSummary(
                        id = "1",
                        label = "Meal 1",
                        description = "Scrambled eggs, spinach, avocado",
                        photoToken = null,
                        lovedIt = false,
                    ),
                ),
                modifier = Modifier.padding(DSSpacing.space7),
            )
        }
    }
}
