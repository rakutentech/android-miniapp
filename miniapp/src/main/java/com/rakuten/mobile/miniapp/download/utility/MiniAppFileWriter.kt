package com.rakuten.mobile.miniapp.download.utility

import android.content.Context
import com.rakuten.mobile.miniapp.core.utils.LocalUrlParser
import com.rakuten.mobile.miniapp.download.DownloadMiniAppImpl
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * Writing files on the device.
 */
class MiniAppFileWriter {

    /**
     * Dagger injected variable.
     */
    @Inject
    lateinit var localUrlParser: LocalUrlParser
    /**
     * Dagger injected variable.
     */
    @Inject
    lateinit var context: Context

    init {
        DownloadMiniAppImpl.daggerDownloadComponent.inject(this)
    }

    /**
     * Writing file from memory to disk.
     */
    fun writeResponseBodyToDisk(response: ResponseBody, appId: String, fileUrl: String) {

        // Parse appID, versionId, and appropriate directories.
        val versionId = localUrlParser.getVersionId(fileUrl)
        val path = localUrlParser.getFilePath(fileUrl)
        val fileName = localUrlParser.getFileName(fileUrl)

        try {
            val dir = "${context.filesDir?.path}/$MINI_APPS_PATH/$appId/$versionId/$path"

            File(dir).mkdirs()
            val file = File(dir, fileName)

            Timber.tag(TAG).d(file.path)

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val byteArray = ByteArray(FILE_WRITE_BATCH_SIZE)

                var fileSizeDownloaded: Long = 0

                inputStream = response.byteStream()
                outputStream = FileOutputStream(file)

                while (true) {
                    val read = inputStream.read(byteArray)

                    if (read == -1) {
                        break
                    }

                    outputStream.write(byteArray, 0, read)

                    fileSizeDownloaded += read

                    outputStream.flush()
                }

                Timber.tag(TAG).d("writing file size: %d", fileSizeDownloaded)
            } catch (e: IOException) {
                Timber.tag(TAG).d(e.message ?: "error")
            } finally {
                if (inputStream != null) {
                    inputStream.close()
                }

                if (outputStream != null) {
                    outputStream.close()
                }
            }
        } catch (e: IOException) {
            Timber.tag(TAG).d(e.message ?: "error")
        }
    }

    companion object {
        private const val TAG = "Mini_FileWriter"
        private const val MINI_APPS_PATH = "miniapps"
        private const val FILE_WRITE_BATCH_SIZE = 4096
    }
}
