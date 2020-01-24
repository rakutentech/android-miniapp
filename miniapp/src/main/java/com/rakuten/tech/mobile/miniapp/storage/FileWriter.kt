package com.rakuten.tech.mobile.miniapp.storage

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

private const val FILE_WRITE_BATCH_SIZE = 4096

internal class FileWriter {

    suspend fun write(inputStream: InputStream, path: String) {
        withContext(Dispatchers.IO) {
            try {
                File(path).mkdirs()
                val file = File(path)
                var outputStream: OutputStream? = null

                try {
                    val byteArray = ByteArray(FILE_WRITE_BATCH_SIZE)
                    var fileSizeDownloaded: Long = 0
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
                    inputStream.close()
                    outputStream?.close()
                }
            } catch (e: IOException) {
                throw MiniAppSdkException(e)
            }
        }
    }
}
