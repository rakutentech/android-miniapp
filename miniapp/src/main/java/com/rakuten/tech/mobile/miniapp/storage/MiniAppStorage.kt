package com.rakuten.tech.mobile.miniapp.storage

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.InputStream

private const val SUB_DIR_MINIAPP = "miniapp"

internal class MiniAppStorage(
    private val fileWriter: FileWriter,
    private val basePath: File,
    private val urlToFileInfoParser: UrlToFileInfoParser = UrlToFileInfoParser()
) {
    private val hostAppBasePath = basePath.path

    private val miniAppBasePath
        get() = "$hostAppBasePath/$SUB_DIR_MINIAPP/"

    @Suppress("TooGenericExceptionCaught")
    suspend fun saveFile(
        source: String,
        basePath: String,
        inputStream: InputStream
    ) {
        try {
            val fileName = getFileName(source)
            fileWriter.unzip(inputStream, getAbsoluteWritePath(basePath, fileName))
        } catch (error: Exception) {
            // This should not happen unless BE sends in a differently "constructed" URL
            // which differs in logic as that of LocalUrlParser
            throw MiniAppSdkException(error)
        }
    }

    @VisibleForTesting
    fun getAbsoluteWritePath(
        basePath: String,
        fileName: String
    ) = "$basePath/$fileName"

    @VisibleForTesting
    fun getFileName(file: String) = urlToFileInfoParser.getFileName(file)

    @VisibleForTesting
    internal fun getMiniAppPath(appId: String) = "${miniAppBasePath}$appId/"

    fun getMiniAppVersionPath(appId: String, versionId: String) = "${getMiniAppPath(appId)}$versionId"

    @Suppress("TooGenericExceptionCaught", "LongMethod")
    suspend fun removeOutdatedVersionApp(
        appId: String,
        latestVersionId: String,
        appPath: String = getMiniAppPath(appId)
    ) {
        val parentFile = File(appPath)
        if (parentFile.isDirectory && parentFile.listFiles() != null) {
            flow {
                parentFile.listFiles()?.forEach { file ->
                    if (!file.absolutePath.endsWith(latestVersionId))
                        emit(file)
                }
            }.collect { file ->
                try {
                    file.deleteRecursively()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
