package com.rakuten.tech.mobile.miniapp.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal class FileWriter {

    suspend fun write(inputStream: InputStream, path: String) = withContext(Dispatchers.IO) {
        inputStream.toFile(path)
    }
}

internal fun InputStream.toFile(path: String) {
    use { input ->
        File(path).apply {
            parentFile?.mkdirs()
//            outputStream().use { input.copyTo(it) }
        }
    }
}

internal suspend fun ZipInputStream.decompress(basePath: String, fileZipPath: String) =
    withContext(Dispatchers.IO) {
        use { input ->
            File(fileZipPath).apply { parentFile?.mkdirs() }
            val outputFilePath = File(basePath)
            var entry: ZipEntry

            while (input.nextEntry.also { entry = it } != null) {
                val newFile = File(outputFilePath, entry.name)
                if (entry.isDirectory)
                    newFile.mkdir()
                else {
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
            }
        }
}
