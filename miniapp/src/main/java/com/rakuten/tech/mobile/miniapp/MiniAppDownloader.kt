package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage

internal class MiniAppDownloader(
    val storage: MiniAppStorage,
    val apiClient: ApiClient
) {

    suspend fun startDownload(appId: String, versionId: String) {
        try {
            val manifest = fetchManifest(appId, versionId)
            val downloadedPath = downloadMiniApp(appId, versionId, manifest)
        } catch (error: Exception) {
            throw MiniAppSdkException(error)
        }
    }

    suspend fun fetchManifest(
        appId: String,
        versionId: String
    ) = apiClient.fetchFileList(appId, versionId)

    private suspend fun downloadMiniApp(
        appId: String,
        versionId: String,
        manifest: ManifestEntity
    ): String {
        val baseSavePath = storage.getSavePathForApp(appId, versionId)
        for (file in manifest.files) {
            val response = apiClient.downloadFile(file)
            val filePath = storage.getFilePathFromUrl(file)
            val fileName = storage.getFileName(file)
            storage.saveFile(response.byteStream(), baseSavePath, filePath, fileName)
        }
        return baseSavePath
    }
}
