package com.rakuten.tech.mobile.miniapp.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

internal class FileWriter {

    suspend fun write(inputStream: InputStream, path: String) {
        withContext(Dispatchers.IO) {
            val file = File(path)
            file.parentFile?.mkdirs()
            inputStream.use { it.copyTo(file.outputStream()) }
        }
    }
}
