package com.rakuten.tech.mobile.miniapp.storage

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

private const val SUB_DIR_MINIAPP = "miniapp"

internal class MiniAppStorage(
    private val fileWriter: FileWriter,
    private val basePath: File,
    private val urlToFileInfoParser: UrlToFileInfoParser = UrlToFileInfoParser()
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

    @VisibleForTesting
    fun getAbsoluteWritePath(
        basePath: String,
        filePath: String,
        fileName: String
    ) = "$basePath$filePath$fileName"

    @VisibleForTesting
    fun getFilePath(file: String) = urlToFileInfoParser.getFilePath(file)

    @VisibleForTesting
    fun getFileName(file: String) = urlToFileInfoParser.getFileName(file)

    @VisibleForTesting
    internal fun getParentPathApp(appId: String) = "${basePath.path}/$SUB_DIR_MINIAPP/$appId/"

    fun getSavePathForApp(appId: String, versionId: String) = "${getParentPathApp(appId)}$versionId"

    suspend fun removeOutdatedVersionApp(
        appId: String,
        versionId: String,
        appPath: String = getParentPathApp(appId)
    ) =
            withContext(Dispatchers.IO) {
        val parentFile = File(appPath)
        if (parentFile.isDirectory && parentFile.listFiles() != null) {
            flow {
                parentFile.listFiles()?.forEach { file ->
                    if (!file.absolutePath.endsWith(versionId))
                        emit(file)
                }
            }.collect { file -> file.deleteRecursively() }
        }
    }
}
