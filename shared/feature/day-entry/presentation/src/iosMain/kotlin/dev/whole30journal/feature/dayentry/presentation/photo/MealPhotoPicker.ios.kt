package dev.whole30journal.feature.dayentry.presentation.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.writeToFile
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeJPEG
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberMealPhotoPicker(onPhotoSave: (String) -> Unit): MealPhotoPicker {
    val presenter = LocalUIViewController.current
    return remember { IosMealPhotoPicker(presenter, onPhotoSave) }
}

@Composable
actual fun rememberMealPhotoResolver(): (String) -> String = remember { { filename -> "${mealPhotosDirectory()}/$filename" } }

private class IosMealPhotoPicker(
    private val presenter: UIViewController,
    private val onPhotoSave: (String) -> Unit,
) : MealPhotoPicker {

    private var activeDelegate: NSObject? = null

    override fun launchCamera() {
        val sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        if (!UIImagePickerController.isSourceTypeAvailable(sourceType)) return

        val delegate = CameraDelegate(::handleResult)
        activeDelegate = delegate
        val picker = UIImagePickerController()
        picker.sourceType = sourceType
        picker.delegate = delegate
        presenter.presentViewController(picker, animated = true, completion = null)
    }

    override fun launchLibrary() {
        val delegate = LibraryDelegate(::handleResult)
        activeDelegate = delegate
        val configuration = PHPickerConfiguration()
        configuration.filter = PHPickerFilter.imagesFilter()
        val picker = PHPickerViewController(configuration = configuration)
        picker.delegate = delegate
        presenter.presentViewController(picker, animated = true, completion = null)
    }

    private fun handleResult(data: NSData?) {
        activeDelegate = null
        val path = data?.let(::writeMealPhoto) ?: return
        onPhotoSave(path)
    }
}

private class CameraDelegate(
    private val onResult: (NSData?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val data = image?.let { UIImageJPEGRepresentation(it, 0.9) }
        picker.dismissViewControllerAnimated(true) { onResult(data) }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) { onResult(null) }
    }
}

private class LibraryDelegate(
    private val onResult: (NSData?) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        val provider = (didFinishPicking.firstOrNull() as? PHPickerResult)?.itemProvider
        picker.dismissViewControllerAnimated(true) {
            when {
                provider == null -> onResult(null)
                provider.hasItemConformingToTypeIdentifier(UTTypeJPEG.identifier) ->
                    provider.loadDataRepresentationForTypeIdentifier(UTTypeJPEG.identifier) { data, _ ->
                        dispatch_async(dispatch_get_main_queue()) { onResult(data) }
                    }
                provider.hasItemConformingToTypeIdentifier(UTTypeImage.identifier) ->
                    provider.loadDataRepresentationForTypeIdentifier(UTTypeImage.identifier) { data, _ ->
                        val jpegData = data?.let { UIImage(data = it) }?.let { UIImageJPEGRepresentation(it, 0.9) }
                        dispatch_async(dispatch_get_main_queue()) { onResult(jpegData) }
                    }
                else -> onResult(null)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun writeMealPhoto(data: NSData): String? {
    val filename = "${NSUUID().UUIDString}.jpg"
    val path = "${mealPhotosDirectory()}/$filename"
    return if (data.writeToFile(path, atomically = true)) filename else null
}

@OptIn(ExperimentalForeignApi::class)
private fun mealPhotosDirectory(): String {
    val documentsPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        .firstOrNull() as? String
        ?: NSTemporaryDirectory()
    val directory = "$documentsPath/meal-photos"
    NSFileManager.defaultManager.createDirectoryAtPath(directory, withIntermediateDirectories = true, attributes = null, error = null)
    return directory
}
