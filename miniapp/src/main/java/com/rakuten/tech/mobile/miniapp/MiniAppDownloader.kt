package com.rakuten.tech.mobile.miniapp

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.storage.MiniAppSharedPreferences
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal class MiniAppDownloader(
    val storage: MiniAppStorage,
    val apiClient: ApiClient
) {

    suspend fun getMiniApp(appId: String, versionId: String): String {
        if (MiniAppSharedPreferences.isAppExisted(appId, versionId)) {
            val baseSavePath = storage.getSavePathForApp(appId, versionId)
            if (File(baseSavePath).exists())
                return baseSavePath
        }
        return startDownload(appId, versionId)
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
            // If backend functions correctly, this should never happen
            isManifestValid(manifest) -> {
                for (file in manifest.files) {
                    val response = apiClient.downloadFile(file)
                    storage.saveFile(file, baseSavePath, response.byteStream())
                }
                registerStorage(appId, versionId)
                return baseSavePath
            }
            else -> throw MiniAppSdkException("Internal Server Error")
        }
    }

    private suspend fun registerStorage(appId: String, versionId: String) = withContext(Dispatchers.IO) {
        MiniAppSharedPreferences.setAppExisted(appId, versionId, true)
    }

    @Suppress("SENSELESS_COMPARISON")
    @VisibleForTesting
    internal fun isManifestValid(manifest: ManifestEntity) =
        manifest != null && manifest.files != null && !manifest.files.isEmpty()
}
