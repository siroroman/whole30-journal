package dev.whole30journal.feature.dayentry.presentation.photo

import androidx.compose.runtime.Composable

interface MealPhotoPicker {
    fun launchCamera()
    fun launchLibrary()
}

@Composable
expect fun rememberMealPhotoPicker(onPhotoSave: (String) -> Unit): MealPhotoPicker

@Composable
expect fun rememberMealPhotoResolver(): (String) -> String
