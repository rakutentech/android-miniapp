package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

private const val TAG = "WebViewFileDownloader"

internal class WebViewFileDownloader(
    private val context: Context,
    private val cache: File,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton(),
    private val fileProvider: DownloadedFileProvider = DownloadedFileProvider(context)
) {
    fun onDownloadStart(url: String, mimetype: String, onUnsupportedFile: () -> Unit) {
        if (url.startsWith("data:")) {
            val decodedBytes = try {
                Base64.decode(
                    url.substring(url.indexOf(",") + 1),
                    Base64.DEFAULT
                )
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Could not share the downloaded file: failed to decode the file as Base64.", e)
                return
            }

            val dir = File(cache, System.currentTimeMillis().toString())
            dir.mkdirs()
            val fileExtension = mimeTypeMap.getExtensionFromMimeType(mimetype)?.let { ".$it" } ?: ""
            val fileName = "download$fileExtension"
            val file = File(dir, fileName)
            try {
                file.writeBytes(decodedBytes)
            } catch (e: IOException) {
                Log.e(TAG, "Could not share the downloaded file: failed to write temporary file to cache.", e)
                return
            }

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = mimetype
            intent.putExtra(Intent.EXTRA_STREAM, fileProvider.getUriForFile(file))

            context.startActivity(Intent.createChooser(intent, null))
        } else {
            onUnsupportedFile()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun cleanup() {
        scope.launch {
            try {
                cache.deleteRecursively()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete downloaded file cache.", e)
            }
        }
    }
}

internal class DownloadedFileProvider(
    private val context: Context
) {
    fun getUriForFile(file: File): Uri = FileProvider.getUriForFile(
        context,
        context.applicationContext.packageName.toString() + ".miniapp.downloadedfileprovider",
        file
    )
}
