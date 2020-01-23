package com.rakuten.tech.mobile.miniapp.storage

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

private const val FILE_WRITE_BATCH_SIZE = 4096

internal class FileWriter {

    suspend fun write(response: ResponseBody, path: String) {
        try {
            File(path).mkdirs()
            val file = File(path)
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
            } catch (e: IOException) {
                throw MiniAppSdkException(e)
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            throw MiniAppSdkException(e)
        }
    }
}
