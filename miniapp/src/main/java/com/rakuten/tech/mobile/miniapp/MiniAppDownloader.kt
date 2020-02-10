package com.rakuten.tech.mobile.miniapp

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage

internal class MiniAppDownloader(
    val storage: MiniAppStorage,
    val apiClient: ApiClient
) {

    suspend fun startDownload(appId: String, versionId: String): String {
        val manifest = fetchManifest(appId, versionId)
        return downloadMiniApp(appId, versionId, manifest)
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
        when {
            // If backend functions correctly, this should never happen
            isManifestValid(manifest) -> {
                for (file in manifest.files) {
                    val response = apiClient.downloadFile(file)
                    storage.saveFile(file, baseSavePath, response.byteStream())
                }
                return baseSavePath
            }
            else -> throw MiniAppSdkException("Internal Server Error")
        }
    }

    @Suppress("SENSELESS_COMPARISON")
    @VisibleForTesting
    fun isManifestValid(manifest: ManifestEntity) =
        manifest != null && manifest.files != null && !manifest.files.isEmpty()
}
