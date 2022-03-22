package com.rakuten.tech.mobile.miniapp.file

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.display.DefaultFileProvider
import com.rakuten.tech.mobile.miniapp.js.FileDownloadCallbackObj
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
import java.io.IOException

@Suppress("TooManyFunctions")
internal class MiniAppFileDownloader {
    @VisibleForTesting
    internal lateinit var activity: Activity
    @VisibleForTesting
    internal lateinit var cache: File
    @VisibleForTesting
    internal var mimeTypeMap = MimeTypeMap.getSingleton()
    @VisibleForTesting
    internal lateinit var bridgeExecutor: MiniAppBridgeExecutor
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun setBridgeExecutor(activity: Activity, bridgeExecutor: MiniAppBridgeExecutor) {
        this.activity = activity
        this.bridgeExecutor = bridgeExecutor
    }

    @VisibleForTesting
    internal fun <T> whenReady(callback: () -> T) {
        if (this::bridgeExecutor.isInitialized && this::activity.isInitialized) {
            callback.invoke()
        }
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    internal fun onFileDownload(callbackId: String, jsonStr: String) = whenReady {
        val callbackObj: FileDownloadCallbackObj? = createFileDownloadCallbackObj(jsonStr)
        if (callbackObj != null) {
            onStartFileDownload(callbackObj)
        } else {
            bridgeExecutor.postError(callbackId, "$ERR_FILE_DOWNLOAD $ERR_WRONG_JSON_FORMAT")
        }
    }

    @VisibleForTesting
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    internal fun createFileDownloadCallbackObj(jsonStr: String): FileDownloadCallbackObj? = try {
        Gson().fromJson(jsonStr, FileDownloadCallbackObj::class.java)
    } catch (e: Exception) {
        null
    }

    @VisibleForTesting
    internal fun onStartFileDownload(callbackObj: FileDownloadCallbackObj) {
        val fileName = callbackObj.param?.filename ?: ""
        val url = callbackObj.param?.url ?: ""
        val headers = callbackObj.param?.headers
        scope.launch {
            startDownloading(callbackObj.id, fileName, url, headers)
        }
    }

    @VisibleForTesting
    internal fun startDownloading(
        callbackId: String,
        fileName: String,
        url: String,
        headers: DownloadFileHeaderObj?,
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
                bridgeExecutor.postValue(callbackId, fileName)
            } ?: run {
                bridgeExecutor.postError(callbackId, "$ERR_FILE_DOWNLOAD $ERR_EMPTY_RESPONSE_BODY")
            }
        } else {
            bridgeExecutor.postError(callbackId, "$ERR_FILE_DOWNLOAD ${response.code}")
        }
    }

    @VisibleForTesting
    internal fun createHttpClient(headers: DownloadFileHeaderObj?): OkHttpClient {
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

    @VisibleForTesting
    internal fun createFileDirectory(fileName: String): File {
        val cacheDir = File("${activity.cacheDir}/mini_app_download")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        cache = cacheDir
        return File(cacheDir, fileName)
    }

    @VisibleForTesting
    @Suppress("NestedBlockDepth", "MagicNumber")
    internal fun writeInputStreamToFile(inputStream: InputStream, file: File) = try {
        inputStream.use { input ->
            var size: Int
            val buffer = ByteArray(2048)
            FileOutputStream(file).use { fos ->
                BufferedOutputStream(fos, buffer.size).use { bos ->
                    while (input.read(buffer, 0, buffer.size)
                            .also { size = it } != -1
                    ) {
                        bos.write(buffer, 0, size)
                    }
                    bos.flush()
                }
            }
        }
    } catch (e: IOException) {
        Log.e(TAG, "Failed to write in the directory.", e)
    }

    @VisibleForTesting
    internal fun openShareIntent(mimetype: String?, file: File) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = mimetype
        intent.putExtra(
            Intent.EXTRA_STREAM,
            DefaultFileProvider(activity).getUriForFile(file)
        )
        activity.startActivity(Intent.createChooser(intent, null))
    }

    @Suppress("TooGenericExceptionCaught")
    @VisibleForTesting
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
        const val ERR_WRONG_JSON_FORMAT = "Can not parse file download json object"
    }
}
