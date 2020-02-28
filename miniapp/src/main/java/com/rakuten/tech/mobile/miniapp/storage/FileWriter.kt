package com.rakuten.tech.mobile.miniapp.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

internal class FileWriter {

    suspend fun write(inputStream: InputStream, path: String) = withContext(Dispatchers.IO) {
        inputStream.toFile(path)
    }
}

internal fun InputStream.toFile(path: String) {
    use { input ->
        File(path).apply {
            parentFile?.mkdirs()
            outputStream().use { input.copyTo(it) }
        }
    }
}
