package dev.whole30journal.feature.dayentry.presentation.photo

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

@Composable
actual fun rememberMealPhotoPicker(onPhotoSave: (String) -> Unit): MealPhotoPicker {
    val context = LocalContext.current
    val pendingCameraFile = remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCameraFile.value?.let { onPhotoSave(it.absolutePath) }
    }
    val libraryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onPhotoSave(copyToMealPhotosDir(context, it).absolutePath) }
    }

    return remember {
        object : MealPhotoPicker {
            override fun launchCamera() {
                val file = createMealPhotoFile(context)
                pendingCameraFile.value = file
                cameraLauncher.launch(fileProviderUri(context, file))
            }

            override fun launchLibrary() {
                libraryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }
}

private fun mealPhotosDir(context: Context): File = File(context.filesDir, "meal-photos").apply { mkdirs() }

private fun createMealPhotoFile(context: Context): File = File(mealPhotosDir(context), "${UUID.randomUUID()}.jpg")

private fun fileProviderUri(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

private fun copyToMealPhotosDir(context: Context, source: Uri): File {
    val destination = createMealPhotoFile(context)
    context.contentResolver.openInputStream(source)?.use { input ->
        destination.outputStream().use { output -> input.copyTo(output) }
    }
    return destination
}
