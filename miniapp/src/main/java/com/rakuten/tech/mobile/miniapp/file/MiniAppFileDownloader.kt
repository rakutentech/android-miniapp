package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Base64
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
import java.io.InputStream

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
@SuppressWarnings("LargeClass")
class MiniAppFileDownloaderDefault(var activity: Activity, var requestCode: Int) : MiniAppFileDownloader {
    private var okHttpClient: OkHttpClient? = null
    @VisibleForTesting
    internal var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
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
        if (url.isHttpUrl) {
            scope.launch {
                val client = okHttpClient ?: OkHttpClient.Builder().build().apply { okHttpClient = this }
                val request = createRequest(url, headers)
                startDownloading(
                    destinationUri = destinationUri,
                    fileName = fileName,
                    client = client,
                    request = request,
                    onDownloadSuccess = onDownloadSuccess,
                    onDownloadFailed = onDownloadFailed
                )
            }
        } else if (url.isDataUri) {
            saveDataUri(
                destinationUri = destinationUri,
                url = url,
                fileName = fileName,
                onDownloadSuccess = onDownloadSuccess,
                onDownloadFailed = onDownloadFailed
            )
        } else {
            onDownloadFailed.invoke(MiniAppDownloadFileError.invalidUrlError)
        }
    }

    @VisibleForTesting
    @Suppress("SwallowedException", "NestedBlockDepth", "LongParameterList")
    internal fun startDownloading(
        destinationUri: Uri,
        fileName: String,
        client: OkHttpClient,
        request: Request,
        onDownloadSuccess: (String) -> Unit,
        onDownloadFailed: (MiniAppDownloadFileError) -> Unit
    ) = try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            response.body?.use { responseBody ->
                saveFile(
                    destinationUri = destinationUri,
                    data = responseBody.byteStream(),
                    fileName = fileName,
                    onDownloadSuccess = onDownloadSuccess,
                    onDownloadFailed = onDownloadFailed
                )
            } ?: run {
                onDownloadFailed.invoke(MiniAppDownloadFileError.downloadFailedError)
            }
        } else {
            onDownloadFailed.invoke(MiniAppDownloadFileError.httpError(response.code, response.message))
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
        if (!url.isValidUrl) {
            onDownloadFailed.invoke(MiniAppDownloadFileError.invalidUrlError)
            return
        }

        this.fileName = fileName
        this.url = url
        this.headers = headers
        this.onDownloadSuccess = onDownloadSuccess
        this.onDownloadFailed = onDownloadFailed

        openCreateDocIntent(activity, fileName)
    }

    /**
     * Can be used when HostApp wants to cancel the file download operation.
     */
    fun onCancel() {
        if (this::onDownloadSuccess.isInitialized)
            onDownloadSuccess.invoke("null")
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

        // Intent.ACTION_CREATE_DOCUMENT creates files with 0 bytes
        // while specifying mimetype in Android API 29 platform,
        // It needs to set "*/*" to prevent this issue.
        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            "*/*"
        } else {
            if (!mimetype.isNullOrBlank())
                mimetype
            else
                "text/plain"
        }
    }

    @VisibleForTesting
    internal fun createRequest(url: String, headers: Map<String, String>): Request {
        val builder = Request.Builder()
        headers.forEach { header ->
            builder.addHeader(header.key, header.value)
        }
        return builder.url(url).build()
    }

    private val String.isHttpUrl get() = this.startsWith("https:") || this.startsWith("http:")
    private val String.isDataUri get() = this.startsWith("data:")
    private val String.isValidUrl get() = this.isHttpUrl || this.isDataUri

    private fun saveDataUri(
        destinationUri: Uri,
        url: String,
        fileName: String,
        onDownloadSuccess: (String) -> Unit,
        onDownloadFailed: (MiniAppDownloadFileError) -> Unit
    ) {
        try {
            val decodedBytes = Base64.decode(
                url.substring(url.indexOf(",") + 1),
                Base64.DEFAULT
            )
            saveFile(
                destinationUri = destinationUri,
                data = decodedBytes.inputStream(),
                fileName = fileName,
                onDownloadSuccess = onDownloadSuccess,
                onDownloadFailed = onDownloadFailed
            )
        } catch (e: IllegalArgumentException) {
            Log.e("MiniAppFileDownloader", "Failed to download file: Error occurred while " +
                    "decoding the Base64 string URI.")
            onDownloadFailed.invoke(MiniAppDownloadFileError.saveFailureError)
        }
    }

    private fun saveFile(
        destinationUri: Uri,
        data: InputStream,
        fileName: String,
        onDownloadSuccess: (String) -> Unit,
        onDownloadFailed: (MiniAppDownloadFileError) -> Unit
    ) {
        activity.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
            data.copyTo(outputStream)
            outputStream.close()
            onDownloadSuccess.invoke(fileName)
        } ?: run {
            onDownloadFailed.invoke(MiniAppDownloadFileError.saveFailureError)
            Log.e("MiniAppFileDownloader", "Failed to download file: Error occurred while " +
                    "opening the OutputStream to download the file.")
        }
    }
}
