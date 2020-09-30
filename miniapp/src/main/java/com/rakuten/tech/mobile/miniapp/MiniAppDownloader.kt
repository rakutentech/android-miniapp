package com.rakuten.tech.mobile.miniapp

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.api.UpdatableApiClient
import com.rakuten.tech.mobile.miniapp.storage.CachedMiniAppVerifier
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import java.io.File

internal class MiniAppDownloader(
    private val storage: MiniAppStorage,
    private var apiClient: ApiClient,
    private val miniAppStatus: MiniAppStatus,
    private val verifier: CachedMiniAppVerifier,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UpdatableApiClient {

    @Suppress("SwallowedException", "LongMethod")
    suspend fun getMiniApp(appId: String): Pair<String, MiniAppInfo> {
        try {
            val miniAppInfo = apiClient.fetchInfo(appId)
            val downloadedVersionPath = retrieveDownloadedVersionPath(miniAppInfo)

            return if (downloadedVersionPath == null) {
                val versionPath = startDownload(miniAppInfo)
                verifier.storeHashAsync(miniAppInfo.id, File(versionPath))
                storeDownloadedMiniApp(miniAppInfo)

                Pair(versionPath, miniAppInfo)
            } else {
                Pair(downloadedVersionPath, miniAppInfo)
            }
        } catch (netError: MiniAppNetException) {
            // load local if possible when offline
            val miniAppInfo = miniAppStatus.getDownloadedMiniApp(appId)
            if (miniAppInfo != null) {
                val downloadedVersionPath = retrieveDownloadedVersionPath(miniAppInfo)
                if (downloadedVersionPath !== null) {
                    return Pair(downloadedVersionPath, miniAppInfo)
                }
            }
        }
        // cannot load miniapp from server
        throw sdkExceptionForInternalServerError()
    }

    private fun storeDownloadedMiniApp(miniAppInfo: MiniAppInfo) {
        miniAppStatus.setVersionDownloaded(miniAppInfo.id, miniAppInfo.version.versionId, true)
        miniAppStatus.saveDownloadedMiniApp(miniAppInfo)
    }

    private fun retrieveDownloadedVersionPath(miniAppInfo: MiniAppInfo): String? {
        val versionPath = storage.getMiniAppVersionPath(miniAppInfo.id, miniAppInfo.version.versionId)

        if (miniAppStatus.isVersionDownloaded(miniAppInfo.id, miniAppInfo.version.versionId, versionPath)) {
            return if (verifier.verify(miniAppInfo.id, File(versionPath))) {
                versionPath
            } else {
                Log.e(TAG, "Failed to verify the hash of the cached files. " +
                        "The files will be deleted and the Mini App re-downloaded.")
                storage.removeApp(miniAppInfo.id)
                miniAppStatus.setVersionDownloaded(miniAppInfo.id, miniAppInfo.version.versionId, false)
                null
            }
        }

        return null
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

    fun getDownloadedMiniAppList(): List<MiniAppInfo> =
        miniAppStatus.getDownloadedMiniAppList()

    @Suppress("SENSELESS_COMPARISON")
    @VisibleForTesting
    internal fun isManifestValid(manifest: ManifestEntity) =
        manifest != null && manifest.files != null && manifest.files.isNotEmpty()

    override fun updateApiClient(apiClient: ApiClient) {
        this.apiClient = apiClient
    }

    companion object {
        private const val TAG = "MiniAppDownloader"
    }
}
