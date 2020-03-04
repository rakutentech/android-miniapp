package com.rakuten.tech.mobile.miniapp

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage

internal class MiniAppDownloader(
    private val storage: MiniAppStorage,
    private val apiClient: ApiClient,
    private val miniAppStatus: MiniAppStatus
) {

    suspend fun getMiniApp(appId: String, versionId: String): String = when {
        miniAppStatus.isVersionDownloaded(appId, versionId) -> storage.getSavePathForApp(appId, versionId)
        else -> startDownload(appId, versionId)
    }

    @VisibleForTesting
    suspend fun startDownload(appId: String, versionId: String): String {
        val manifest = fetchManifest(appId, versionId)
        return downloadMiniApp(appId, versionId, manifest)
    }

    @VisibleForTesting
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
            isManifestValid(manifest) -> {
                for (file in manifest.files) {
                    val response = apiClient.downloadFile(file)
                    storage.saveFile(file, baseSavePath, response.byteStream())
                }
                miniAppStatus.setVersionDownloaded(appId, versionId, true)
                return baseSavePath
            }
            // If backend functions correctly, this should never happen
            else -> throw sdkExceptionForInternalServerError()
        }
    }

    @Suppress("SENSELESS_COMPARISON")
    @VisibleForTesting
    internal fun isManifestValid(manifest: ManifestEntity) =
        manifest != null && manifest.files != null && manifest.files.isNotEmpty()
}
