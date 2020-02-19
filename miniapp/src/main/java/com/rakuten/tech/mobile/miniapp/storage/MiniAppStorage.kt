package com.rakuten.tech.mobile.miniapp.storage

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.legacy.core.utils.LocalUrlParser
import java.io.File
import java.io.InputStream

private const val SUB_DIR_MINIAPP = "miniapp"

internal class MiniAppStorage(
    val fileWriter: FileWriter,
    val basePath: File,
    val localUrlParser: LocalUrlParser = LocalUrlParser()
) {

    @Suppress("TooGenericExceptionCaught")
    suspend fun saveFile(
        source: String,
        basePath: String,
        inputStream: InputStream
    ) {
        try {
            val filePath = getFilePath(source)
            val fileName = getFileName(source)
            fileWriter.write(inputStream, getAbsoluteWritePath(basePath, filePath, fileName))
        } catch (error: Exception) {
            // This should not happen unless BE sends in a differently "constructed" URL
            // which differs in logic as that of LocalUrlParser
            throw MiniAppSdkException(error)
        }
    }

    fun getAbsoluteWritePath(
        basePath: String,
        filePath: String,
        fileName: String
    ) = "$basePath$filePath$fileName"

    fun getFilePath(file: String) = localUrlParser.getFilePath(file)

    fun getFileName(file: String) = localUrlParser.getFileName(file)

    fun getSavePathForApp(appId: String, versionId: String) =
        "${basePath.path}/$SUB_DIR_MINIAPP/$appId/$versionId"

    fun filePathExists(baseSavePath: String): Boolean = File(baseSavePath).exists()
}
