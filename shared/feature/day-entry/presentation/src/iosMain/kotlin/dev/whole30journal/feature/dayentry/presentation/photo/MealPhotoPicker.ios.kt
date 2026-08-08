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
import platform.UniformTypeIdentifiers.UTTypeJPEG
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberMealPhotoPicker(onPhotoSave: (String) -> Unit): MealPhotoPicker {
    val presenter = LocalUIViewController.current
    return remember { IosMealPhotoPicker(presenter, onPhotoSave) }
}

private class IosMealPhotoPicker(
    private val presenter: UIViewController,
    private val onPhotoSave: (String) -> Unit,
) : MealPhotoPicker {

    private var activeDelegate: NSObject? = null

    override fun launchCamera() {
        val delegate = CameraDelegate(::handleResult)
        activeDelegate = delegate
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
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
        picker.dismissViewControllerAnimated(true, completion = null)
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        onResult(image?.let { UIImageJPEGRepresentation(it, 0.9) })
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onResult(null)
    }
}

private class LibraryDelegate(
    private val onResult: (NSData?) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val provider = (didFinishPicking.firstOrNull() as? PHPickerResult)?.itemProvider
        if (provider != null && provider.hasItemConformingToTypeIdentifier(UTTypeJPEG.identifier)) {
            provider.loadDataRepresentationForTypeIdentifier(UTTypeJPEG.identifier) { data, _ ->
                dispatch_async(dispatch_get_main_queue()) { onResult(data) }
            }
        } else {
            onResult(null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun writeMealPhoto(data: NSData): String? {
    val path = "${mealPhotosDirectory()}/${NSUUID().UUIDString}.jpg"
    return if (data.writeToFile(path, atomically = true)) path else null
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
