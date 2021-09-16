package com.rakuten.tech.mobile.miniapp.signatureverifier

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

private const val SUB_DIR_MINIAPP_ZIP = "miniapp/temp_zip/"

internal class MiniAppFileUtil(val basePath: File) {
    private val hostAppBasePath = basePath.path
    private val miniAppZipBasePath
        get() = "$hostAppBasePath/$SUB_DIR_MINIAPP_ZIP/"
    private val zipFileName = "temp-ma-bundle.zip"

    @SuppressWarnings("TooGenericExceptionCaught", "MagicNumber", "PrintStackTrace", "NestedBlockDepth")
    fun createFile(inputStream: InputStream): File {
        val newFile = File(miniAppZipBasePath, zipFileName).apply { parentFile?.mkdirs() }
        try {
            inputStream.use { input ->
                val outputStream = FileOutputStream(newFile)
                outputStream.use { output ->
                    val buffer = ByteArray(4 * 1024)
                    while (true) {
                        val byteCount = input.read(buffer)
                        if (byteCount < 0) break
                        output.write(buffer, 0, byteCount)
                    }
                    output.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return newFile
    }
}
