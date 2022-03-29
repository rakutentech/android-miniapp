package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.errors.MiniAppDownloadFileError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * The file downloader of a miniapp with `onStartFileDownload` function.
 * To start file download on the device.
 **/
interface MiniAppFileDownloader {
    /**
     * For downloading the files which has been invoked by miniapp.
     * @param fileName return the name of the file.
     * @param url return the download url of the file.
     * @param headers returns the header of the file if there is any.
     * @param onDownloadSuccess contains file name send from host app to miniapp.
     * @param onDownloadFailed contains custom error message send from host app.
     **/
    fun onStartFileDownload(
        fileName: String,
        url: String,
        headers: Map<String, String>,
        onDownloadSuccess: (String) -> Unit,
        onDownloadFailed: (MiniAppDownloadFileError) -> Unit
    )
}

/**
 * The default file downloader of a miniapp.
 * @param requestCode of file downloading using an intent inside sdk, which will also be used
 * to retrieve the Uri of the file by [Activity.onActivityResult] in the HostApp.
 **/
class MiniAppFileDownloaderDefault(var activity: Activity, var requestCode: Int) : MiniAppFileDownloader {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var okHttpClient: OkHttpClient? = null
    @VisibleForTesting
    internal lateinit var fileName: String
    @VisibleForTesting
    internal lateinit var url: String
    @VisibleForTesting
    internal lateinit var headers: Map<String, String>
    @VisibleForTesting
    internal lateinit var onDownloadSuccess: (String) -> Unit
    @VisibleForTesting
    internal lateinit var onDownloadFailed: (MiniAppDownloadFileError) -> Unit

    /**
     * Retrieve the Uri of the file by [Activity.onActivityResult] in the HostApp.
     **/
    fun onReceivedResult(destinationUri: Uri) {
        val client = okHttpClient ?: OkHttpClient.Builder().build().apply { okHttpClient = this }
        var request = createRequest(url, headers)
        scope.launch {
            startDownloading(destinationUri, client, request, onDownloadSuccess, onDownloadFailed)
        }
    }

    @VisibleForTesting
    @Suppress("SwallowedException", "NestedBlockDepth")
    internal fun startDownloading(
        destinationUri: Uri,
        client: OkHttpClient,
        request: Request,
        onDownloadSuccess: (String) -> Unit,
        onDownloadFailed: (MiniAppDownloadFileError) -> Unit
    ) = try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            response.body?.use { responseBody ->
                activity.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    responseBody.byteStream().copyTo(outputStream)
                    outputStream.close()
                    onDownloadSuccess.invoke(fileName)
                } ?: {
                    onDownloadFailed.invoke(MiniAppDownloadFileError.saveFailureError)
                    Log.e("Downloader", "Failed to download file: could not open OutputStream.")
                }
            } ?: run {
                onDownloadFailed.invoke(MiniAppDownloadFileError.downloadFailedError)
            }
        } else {
            onDownloadFailed.invoke(MiniAppDownloadFileError.custom(response.code.toString(), response.message))
        }
    } catch (e: IOException) {
        onDownloadFailed.invoke(MiniAppDownloadFileError.downloadFailedError)
    }

    override fun onStartFileDownload(
        fileName: String,
        url: String,
        headers: Map<String, String>,
        onDownloadSuccess: (String) -> Unit,
        onDownloadFailed: (MiniAppDownloadFileError) -> Unit
    ) {
        this.fileName = fileName
        this.url = url
        this.headers = headers
        this.onDownloadSuccess = onDownloadSuccess
        this.onDownloadFailed = onDownloadFailed

        openCreateDocIntent(activity, fileName)
    }

    @VisibleForTesting
    internal fun openCreateDocIntent(activity: Activity, fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = getMimeType(fileName)
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        activity.startActivityForResult(intent, requestCode)
    }

    @VisibleForTesting
    internal fun getMimeType(fileName: String): String {
        val extension = if (fileName.contains('.'))
            fileName.split('.').last()
        else
            ""
        val mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return if (!mimetype.isNullOrBlank())
            mimetype
        else
            "text/plain"
    }

    @VisibleForTesting
    internal fun createRequest(url: String, headers: Map<String, String>): Request {
        val builder = Request.Builder()
        headers?.forEach { header ->
            builder.addHeader(header.key, header.value)
        }
        return builder.url(url).build()
    }
}
