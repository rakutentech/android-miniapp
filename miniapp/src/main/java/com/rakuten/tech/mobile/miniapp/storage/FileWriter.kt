package com.rakuten.tech.mobile.miniapp.storage

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal class FileWriter(private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO) {

    suspend fun unzip(inputStream: InputStream, zipPath: String) = withContext(coroutineDispatcher) {
        ZipInputStream(inputStream).decompress(zipPath)
    }
}

internal fun ZipInputStream.decompress(zipPath: String) = use { input ->
    val outputFilePath = File(zipPath).apply { parentFile?.mkdirs() }
    var entry: ZipEntry?

    while (input.nextEntry.also { entry = it } != null) {
        val newFile = File(outputFilePath.parentFile, entry!!.name)
        newFile.parentFile?.mkdirs()
        if (entry!!.isDirectory)
            newFile.mkdir()
        else
            writeFile(newFile, input)
    }
}

@Suppress("MagicNumber")
private fun writeFile(newFile: File, input: ZipInputStream) {
    var size: Int
    val buffer = ByteArray(2048)
    FileOutputStream(newFile).use { fos ->
        BufferedOutputStream(fos, buffer.size).use { bos ->
            while (input.read(buffer, 0, buffer.size).also { size = it } != -1) {
                bos.write(buffer, 0, size)
            }
            bos.flush()
        }
    }
}
