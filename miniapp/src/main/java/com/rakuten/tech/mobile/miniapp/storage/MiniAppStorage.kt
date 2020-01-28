package com.rakuten.tech.mobile.miniapp.storage

import com.rakuten.tech.mobile.miniapp.legacy.core.utils.LocalUrlParser
import java.io.File
import java.io.InputStream

private const val SUB_DIR_MINIAPP = "miniapp"

@SuppressWarnings("UseDataClass")
internal class MiniAppStorage(
    val fileWriter: FileWriter,
    val basePath: File,
    val localUrlParser: LocalUrlParser = LocalUrlParser()
) {

    suspend fun saveFile(
        source: String,
        basePath: String,
        inputStream: InputStream
    ) {
        val filePath = getFilePath(source)
        val fileName = getFileName(source)
        fileWriter.write(inputStream, getAbsoluteWritePath(basePath, filePath, fileName))
    }

    fun getAbsoluteWritePath(
        basePath: String,
        filePath: String,
        fileName: String
    ) = "$basePath$filePath$fileName"

    fun getFilePath(file: String) = localUrlParser.getFilePath(file)

    fun getFileName(file: String) = localUrlParser.getFileName(file)

    fun getSavePathForApp(appId: String, versionId: String) =
        "${basePath.path}/$SUB_DIR_MINIAPP/$appId/$versionId/"
}
