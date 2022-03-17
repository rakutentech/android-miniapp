package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.display.DefaultFileProvider
import com.rakuten.tech.mobile.miniapp.js.CustomFileDownloadCallbackObj
import com.rakuten.tech.mobile.miniapp.js.DownloadFileHeaderObj
import com.rakuten.tech.mobile.miniapp.js.MiniAppBridgeExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

internal class MiniAppFileDownloader {
    private lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private lateinit var activity: Activity
    private val mimeTypeMap = MimeTypeMap.getSingleton()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var cache: File

    fun setBridgeExecutor(activity: Activity, bridgeExecutor: MiniAppBridgeExecutor) {
        this.activity = activity
        this.bridgeExecutor = bridgeExecutor
    }

    private fun <T> whenReady(callback: () -> T) {
        if (this::bridgeExecutor.isInitialized && this::activity.isInitialized) {
            callback.invoke()
        }
    }

    internal fun onStartFileDownload(callbackObj: CustomFileDownloadCallbackObj) = whenReady {
        val fileName = callbackObj.param?.filename ?: ""
        val url = callbackObj.param?.url ?: ""
        val headers = callbackObj.param?.headers
        val successCallback =
            { fileName: String -> bridgeExecutor.postValue(callbackObj.id, fileName) }
        val errorCallback = { message: String ->
            bridgeExecutor.postError(callbackObj.id, "$ERR_FILE_DOWNLOAD $message")
        }
        scope.launch {
            startDownloading(fileName, url, headers, successCallback, errorCallback)
        }
    }

    private fun startDownloading(
        fileName: String,
        url: String,
        headers: DownloadFileHeaderObj?,
        successCallback: (String) -> Unit,
        errorCallback: (String) -> Unit
    ) {
        val file = createFileDirectory(fileName = fileName)
        val extension = url.substring(url.lastIndexOf("."))
        val mimetype = mimeTypeMap.getMimeTypeFromExtension(extension)
        val client = createHttpClient(headers)
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            response.body?.let { responseBody ->
                writeInputStreamToFile(inputStream = responseBody.byteStream(), file = file)
                openShareIntent(mimetype, file)
                successCallback(fileName)
            } ?: run {
                errorCallback(ERR_EMPTY_RESPONSE_BODY)
            }
        } else {
            errorCallback("Error Code ${response.code}")
        }
    }

    private fun createHttpClient(headers: DownloadFileHeaderObj?): OkHttpClient {
        val builder = OkHttpClient.Builder()
        headers?.token?.let { token ->
            builder.addNetworkInterceptor {
                val requestBuilder = it.request().newBuilder()
                requestBuilder.addHeader("token", token)
                it.proceed(requestBuilder.build())
            }
        }
        return builder.build()
    }

    private fun createFileDirectory(fileName: String): File {
        val cacheDir = File("${activity.cacheDir}/mini_app_download")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        cache = cacheDir
        return File(cacheDir, fileName)
    }

    @Suppress(" NestedBlockDepth", "MagicNumber")
    private fun writeInputStreamToFile(inputStream: InputStream, file: File) {
        inputStream.use { inputStream ->
            var size: Int
            val buffer = ByteArray(2048)
            FileOutputStream(file).use { fos ->
                BufferedOutputStream(fos, buffer.size).use { bos ->
                    while (inputStream.read(buffer, 0, buffer.size)
                            .also { size = it } != -1
                    ) {
                        bos.write(buffer, 0, size)
                    }
                    bos.flush()
                }
            }
        }
    }

    private fun openShareIntent(mimetype: String?, file: File) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = mimetype
        intent.putExtra(
            Intent.EXTRA_STREAM,
            DefaultFileProvider(activity).getUriForFile(file)
        )
        activity.startActivity(Intent.createChooser(intent, null))
    }

    @Suppress("TooGenericExceptionCaught")
    internal fun cleanup() {
        scope.launch {
            try {
                cache.deleteRecursively()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete downloaded file cache.", e)
            }
        }
    }

    @VisibleForTesting
    internal companion object {
        private const val TAG = "MiniAppFileDownloader"
        const val ERR_FILE_DOWNLOAD = "DOWNLOAD FAILED:"
        const val ERR_EMPTY_RESPONSE_BODY = "Empty Response Body"
    }
}
