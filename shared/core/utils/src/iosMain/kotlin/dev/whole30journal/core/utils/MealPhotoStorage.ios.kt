package dev.whole30journal.core.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask

actual class MealPhotoStorage {
    @OptIn(ExperimentalForeignApi::class)
    actual fun deleteAll() {
        val documentsPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
            .firstOrNull() as? String
            ?: NSTemporaryDirectory()
        NSFileManager.defaultManager.removeItemAtPath("$documentsPath/meal-photos", error = null)
    }
}
