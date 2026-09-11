package dev.whole30journal.core.utils

import android.content.Context
import java.io.File

actual class MealPhotoStorage(private val context: Context) {
    actual fun deleteAll() {
        File(context.filesDir, "meal-photos").deleteRecursively()
    }
}
