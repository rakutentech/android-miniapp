package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.annotation.VisibleForTesting

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

    @Suppress("TooGenericExceptionCaught", "SwallowedException", "LongMethod")
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
        } catch (e: Exception) {
            resetCallback()
            Log.e(MiniAppFileChooser::class.java.simpleName, e.message.toString())
            return false
        }
        return true
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
            data != null && clipData == null-> {
                callback?.onReceiveValue((arrayOf(data)))
            }
            clipData != null -> {
                val uriList = mutableListOf<Uri>()
                for (i in 0 until clipData.itemCount) {
                    uriList.add(clipData.getItemAt(i).uri)
                }

                callback?.onReceiveValue((uriList.toTypedArray()))
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
    }
}
