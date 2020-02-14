package com.rakuten.tech.mobile.miniapp.storage

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.legacy.core.utils.LocalUrlParser
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    ) = try {
        coroutineScope {
            val filePath = async { getFilePath(source) }
            val fileName = async { getFileName(source) }
            fileWriter.write(
                inputStream,
                getAbsoluteWritePath(basePath, filePath.await(), fileName.await())
            )
        }
    } catch (error: Exception) {
        // This should not happen unless BE sends in a differently "constructed" URL
        // which differs in logic as that of LocalUrlParser
        throw MiniAppSdkException(error)
    }

    fun getAbsoluteWritePath(
        basePath: String,
        filePath: String,
        fileName: String
    ) = "$basePath$filePath$fileName"

    suspend fun getFilePath(file: String) = localUrlParser.getFilePath(file)

    suspend fun getFileName(file: String) = localUrlParser.getFileName(file)

    fun getSavePathForApp(appId: String, versionId: String) =
        "${basePath.path}/$SUB_DIR_MINIAPP/$appId/$versionId"
}
