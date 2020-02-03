package com.rakuten.tech.mobile.miniapp.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

internal class FileWriter {

    suspend fun write(
        inputStream: InputStream,
        parentPath: String,
        fileName: String
    ) {
        withContext(Dispatchers.IO) {
            File(parentPath).mkdirs()
            val file = File(parentPath, fileName)
            inputStream.use { it.copyTo(file.outputStream()) }
            file.outputStream().close()
        }
    }
}
