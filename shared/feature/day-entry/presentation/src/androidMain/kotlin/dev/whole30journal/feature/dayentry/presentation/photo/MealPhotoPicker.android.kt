package dev.whole30journal.feature.dayentry.presentation.photo

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

@Composable
actual fun rememberMealPhotoPicker(onPhotoSave: (String) -> Unit): MealPhotoPicker {
    val context = LocalContext.current
    val pendingCameraPath = rememberSaveable { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCameraPath.value?.let { onPhotoSave(File(it).name) }
    }
    val libraryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { copyToMealPhotosDir(context, it)?.let { file -> onPhotoSave(file.name) } }
    }

    return remember {
        object : MealPhotoPicker {
            override fun launchCamera() {
                val file = createMealPhotoFile(context)
                pendingCameraPath.value = file.absolutePath
                cameraLauncher.launch(fileProviderUri(context, file))
            }

            override fun launchLibrary() {
                libraryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }
}

@Composable
actual fun rememberMealPhotoResolver(): (String) -> String {
    val context = LocalContext.current
    return remember(context) { { filename -> File(mealPhotosDir(context), filename).absolutePath } }
}

private fun mealPhotosDir(context: Context): File = File(context.filesDir, "meal-photos").apply { mkdirs() }

private fun createMealPhotoFile(context: Context): File = File(mealPhotosDir(context), "${UUID.randomUUID()}.jpg")

private fun fileProviderUri(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

private fun copyToMealPhotosDir(context: Context, source: Uri): File? {
    val destination = createMealPhotoFile(context)
    val copied = context.contentResolver.openInputStream(source)?.use { input ->
        destination.outputStream().use { output -> input.copyTo(output) }
    }
    return destination.takeIf { copied != null }
}
