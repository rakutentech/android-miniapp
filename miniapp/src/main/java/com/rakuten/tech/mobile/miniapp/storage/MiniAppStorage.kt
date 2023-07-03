package com.rakuten.tech.mobile.miniapp.storage

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.InputStream

internal const val SUB_DIR_MINIAPP = "miniapp"

@Suppress("TooManyFunctions")
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

    @Suppress("TooGenericExceptionCaught")
    suspend fun saveFileFromBundle(
        fileName: String,
        appId: String,
        versionId: String,
        inputStream: InputStream
    ): String {
        try {
            val filePath = getBundleWritePath(appId, versionId)
            fileWriter.unzip(
                inputStream,
                getAbsoluteWritePath(
                    filePath,
                    fileName
                )
            )
            return filePath
        } catch (error: Exception) {
            // This should not happen unless BE sends in a differently "constructed" URL
            // which differs in logic as that of LocalUrlParser
            throw MiniAppSdkException(error)
        }
    }

    fun getBundleWritePath(appId: String, versionId: String) =
        "$hostAppBasePath/$SUB_DIR_MINIAPP/$appId/$versionId"

    @VisibleForTesting
    fun getAbsoluteWritePath(
        basePath: String,
        fileName: String
    ) = "$basePath/$fileName"

    @VisibleForTesting
    fun getFileName(file: String) = urlToFileInfoParser.getFileName(file)

    @VisibleForTesting
    internal fun getMiniAppPath(appId: String) = "${miniAppBasePath}$appId/"

    fun getMiniAppVersionPath(appId: String, versionId: String) =
        "${getMiniAppPath(appId)}$versionId"

    fun removeApp(
        appId: String,
        appPath: String = getMiniAppPath(appId)
    ) {
        val parentFile = File(appPath)
        deleteDirectory(parentFile)
    }

    suspend fun removeVersions(
        appId: String,
        exclusiveVersionId: String,
        appPath: String = getMiniAppPath(appId)
    ) {
        val parentFile = File(appPath)
        if (parentFile.isDirectory && parentFile.listFiles() != null) {
            flow {
                parentFile.listFiles()?.forEach { file ->
                    if (!file.absolutePath.endsWith(exclusiveVersionId))
                        emit(file)
                }
            }.collect { file -> deleteDirectory(file) }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    @VisibleForTesting
    internal fun deleteDirectory(file: File) {
        try {
            file.deleteRecursively()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete the directory: $file", e)
        }
    }

    companion object {
        private val TAG = this::class.simpleName
    }

    @Suppress("ExpressionBodySyntax")
    @VisibleForTesting
    internal fun isValidMiniAppInfo(appId: String, versionId: String): Boolean {
        return appId.isNotEmpty() && versionId.isNotEmpty()
    }

    @VisibleForTesting
    internal fun isMiniAppAvailable(appId: String, versionId: String): Boolean {
        val versionPath = getBundleWritePath(appId, versionId)
        return File(versionPath).exists() && File(versionPath).isDirectory
    }
}
