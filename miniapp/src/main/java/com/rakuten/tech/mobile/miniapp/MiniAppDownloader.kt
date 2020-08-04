package com.rakuten.tech.mobile.miniapp

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.api.UpdatableApiClient
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job

internal class MiniAppDownloader(
    private val storage: MiniAppStorage,
    private var apiClient: ApiClient,
    private val miniAppStatus: MiniAppStatus,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UpdatableApiClient {

    @Suppress("SwallowedException")
    suspend fun getMiniApp(appId: String): Pair<String, MiniAppInfo> {
        try {
            val miniAppInfo = apiClient.fetchInfo(appId)
            var versionPath = storage.getMiniAppVersionPath(miniAppInfo.id, miniAppInfo.version.versionId)
            if (!miniAppStatus.isVersionDownloaded(miniAppInfo.id, miniAppInfo.version.versionId, versionPath))
                versionPath = startDownload(miniAppInfo)

            storeDownloadedMiniApp(miniAppInfo)

            return Pair(versionPath, miniAppInfo)
        } catch (netError: MiniAppNetException) {
            // load local if possible when offline
            val miniAppInfo = miniAppStatus.getDownloadedMiniApp(appId)
            if (miniAppInfo != null) {
                val versionPath = storage.getMiniAppVersionPath(appId, miniAppInfo.version.versionId)
                if (miniAppStatus.isVersionDownloaded(appId, miniAppInfo.version.versionId, versionPath))
                    return Pair(versionPath, miniAppInfo)
            }
        }
        // cannot load miniapp from server
        throw sdkExceptionForInternalServerError()
    }

    private fun storeDownloadedMiniApp(miniAppInfo: MiniAppInfo) {
        miniAppStatus.setVersionDownloaded(miniAppInfo.id, miniAppInfo.version.versionId, true)
        miniAppStatus.saveDownloadedMiniApp(miniAppInfo)
    }

    @VisibleForTesting
    suspend fun startDownload(miniAppInfo: MiniAppInfo): String {
        val manifest = fetchManifest(miniAppInfo.id, miniAppInfo.version.versionId)
        return downloadMiniApp(miniAppInfo, manifest)
    }

    @VisibleForTesting
    suspend fun fetchManifest(
        appId: String,
        versionId: String
    ) = apiClient.fetchFileList(appId, versionId)

    @SuppressWarnings("LongMethod")
    private suspend fun downloadMiniApp(miniAppInfo: MiniAppInfo, manifest: ManifestEntity): String {
        val appId = miniAppInfo.id
        val versionId = miniAppInfo.version.versionId
        val baseSavePath = storage.getMiniAppVersionPath(appId, versionId)
        when {
            isManifestValid(manifest) -> {
                for (file in manifest.files) {
                    val response = apiClient.downloadFile(file)
                    storage.saveFile(file, baseSavePath, response.byteStream())
                }
                withContext(coroutineDispatcher) {
                    launch(Job()) { storage.removeOutdatedVersionApp(appId, versionId) }
                }
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

    override fun updateApiClient(apiClient: ApiClient) {
        this.apiClient = apiClient
    }
}
