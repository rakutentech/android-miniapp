package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.annotation.VisibleForTesting
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * The file chooser of a miniapp with `onShowFileChooser` function.
 **/
interface MiniAppFileChooser {

    /**
     * For choosing the files which has been invoked by [WebChromeClient.onShowFileChooser]
     * inside the miniapp webview.
     * @param filePathCallback a callback to provide the array of file-paths to select.
     * @param fileChooserParams the parameters can be used to customize the options of file chooser.
     * @param context the Activity context can be used to start the intent to choose file.
     **/
    fun onShowFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?,
        context: Context
    ): Boolean
}

/**
 * The default file chooser of a miniapp.
 * @param requestCode of file choosing using an intent inside sdk, which will also be used
 * to retrieve the data by [Activity.onActivityResult] in the HostApp.
 **/
class MiniAppFileChooserDefault(var requestCode: Int) : MiniAppFileChooser {

    internal var callback: ValueCallback<Array<Uri>>? = null
    internal var currentPhotoPath: String? = null

    @Suppress("TooGenericExceptionCaught", "SwallowedException", "LongMethod", "NestedBlockDepth")
    override fun onShowFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?,
        context: Context
    ): Boolean {
        try {
            callback = filePathCallback
            val intent = fileChooserParams?.createIntent()
            if (fileChooserParams?.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
                intent?.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            if (fileChooserParams?.isCaptureEnabled == true) {
                dispatchTakePictureIntent(context)
            } else {
                // Uses Intent.EXTRA_MIME_TYPES to pass multiple mime types.
                fileChooserParams?.acceptTypes?.let { acceptTypes ->
                    if (acceptTypes.isNotEmpty() && !(acceptTypes.size == 1 && acceptTypes[0].equals(""))) {
                        // Accept all first.
                        intent?.type = "*/*"
                        // Convert to valid MimeType if with dot.
                        val validMimeTypes = extractValidMimeTypes(acceptTypes).toTypedArray()
                        // filter mime types by Intent.EXTRA_MIME_TYPES.
                        intent?.putExtra(Intent.EXTRA_MIME_TYPES, validMimeTypes)
                    }
                }
                (context as Activity).startActivityForResult(intent, requestCode)
            }
        } catch (e: Exception) {
            resetCallback()
            Log.e(MiniAppFileChooser::class.java.simpleName, e.message.toString())
            return false
        }
        return true
    }

    @Suppress("SwallowedException")
    private fun dispatchTakePictureIntent(context: Context) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            // Create the File where the photo should go
            val photoFile: File? = try {
                createImageFile(context)
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName.toString() + ".miniapp.imagefileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                (context as Activity).startActivityForResult(takePictureIntent, requestCode)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    /**
     * Validation Utility for MimeTypes.
     * @param mimeTypes The Mimetypes needs to convert to valid types.
     */
    @VisibleForTesting
    internal fun extractValidMimeTypes(mimeTypes: Array<String>): List<String> {
        val mtm = MimeTypeMap.getSingleton()
        return mimeTypes.mapNotNull { mime ->
            mime.trim().let {
                if (it.startsWith(".")) {
                    mtm.getMimeTypeFromExtension(it.removePrefix("."))
                } else {
                    if (mtm.hasMimeType(it)) it else null
                }
            }
        }.distinct()
    }

    /**
     * Receive the files from the HostApp.
     * @param intent The data after successfully retrieved by [Activity.onActivityResult] in the HostApp.
     */
    @Suppress("OptionalWhenBraces", "LongMethod")
    fun onReceivedFiles(intent: Intent) {
        val data = intent.data
        val clipData = intent.clipData
        when {
            data != null && clipData == null -> {
                callback?.onReceiveValue((arrayOf(data)))
            }
            clipData != null -> {
                val uriList = mutableListOf<Uri>()
                for (i in 0 until clipData.itemCount) {
                    uriList.add(clipData.getItemAt(i).uri)
                }

                callback?.onReceiveValue((uriList.toTypedArray()))
            }
            currentPhotoPath != null -> {
                var results = mutableListOf<Uri>()
                results.add(Uri.fromFile(File(currentPhotoPath)))
                callback?.onReceiveValue((results.toTypedArray()))
            }
            else -> {
                callback?.onReceiveValue(null)
            }
        }
        resetCallback()
    }

    /**
     * Can be used when HostApp wants to cancel the file choosing operation.
     */
    fun onCancel() {
        callback?.onReceiveValue(null)
        resetCallback()
    }

    @VisibleForTesting
    internal fun resetCallback() {
        callback = null
        currentPhotoPath = null
    }
}
