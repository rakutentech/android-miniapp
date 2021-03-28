package com.rakuten.tech.mobile.miniapp

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.MetadataPermissionObj
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestApiCache
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.api.MetadataEntity
import com.rakuten.tech.mobile.miniapp.api.UpdatableApiClient
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.storage.CachedMiniAppVerifier
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL

@Suppress("SwallowedException", "TooManyFunctions", "LargeClass", "MaxLineLength")
internal class MiniAppDownloader(
    private var apiClient: ApiClient,
    initStorage: () -> MiniAppStorage,
    initStatus: () -> MiniAppStatus,
    initVerifier: () -> CachedMiniAppVerifier,
    initManifestApiCache: () -> ManifestApiCache,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UpdatableApiClient {
    private val storage: MiniAppStorage by lazy { initStorage() }
    private val miniAppStatus: MiniAppStatus by lazy { initStatus() }
    private val verifier: CachedMiniAppVerifier by lazy { initVerifier() }
    private val manifestApiCache: ManifestApiCache by lazy { initManifestApiCache() }

    suspend fun getMiniApp(appId: String): Pair<String, MiniAppInfo> = try {
        val miniAppInfo = apiClient.fetchInfo(appId)
        onGetMiniApp(miniAppInfo)
    } catch (netError: MiniAppNetException) {
        onNetworkError(miniAppStatus.getDownloadedMiniApp(appId))
    }

    suspend fun getMiniApp(miniAppInfo: MiniAppInfo): Pair<String, MiniAppInfo> = try {
        onGetMiniApp(miniAppInfo)
    } catch (netError: MiniAppNetException) {
        onNetworkError(miniAppInfo)
    }

    @Suppress("ThrowsCount", "MagicNumber")
    fun validateHttpAppUrl(url: String) {
        var connection: HttpURLConnection? = null
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            val validHTTPResponseCodes = 100..399
            val code = connection.responseCode
            if (code !in validHTTPResponseCodes) {
                throw MiniAppSdkException("Invalid URL error")
            }
        } catch (netError: ConnectException) {
            throw MiniAppNotFoundException("Invalid URL error")
        } catch (ioError: IOException) {
            throw sdkExceptionForInternalServerError()
        } finally {
            connection?.disconnect()
        }
    }

    private suspend fun onGetMiniApp(miniAppInfo: MiniAppInfo): Pair<String, MiniAppInfo> {
        val downloadedVersionPath = retrieveDownloadedVersionPath(miniAppInfo)

        return if (downloadedVersionPath == null) {
            val versionPath = startDownload(miniAppInfo)
            verifier.storeHashAsync(miniAppInfo.version.versionId, File(versionPath))
            storeDownloadedMiniApp(miniAppInfo)

            Pair(versionPath, miniAppInfo)
        } else {
            Pair(downloadedVersionPath, miniAppInfo)
        }
    }

    private fun onNetworkError(miniAppInfo: MiniAppInfo?): Pair<String, MiniAppInfo> {
        if (miniAppInfo != null) {
            val downloadedVersionPath = retrieveDownloadedVersionPath(miniAppInfo)
            if (downloadedVersionPath !== null) {
                return Pair(downloadedVersionPath, miniAppInfo)
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
            return if (verifier.verify(miniAppInfo.version.versionId, File(versionPath)))
                versionPath
            else {
                Log.e(
                    TAG, "Failed to verify the hash of the cached files. " +
                            "The files will be deleted and the Mini App re-downloaded."
                )
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

    @Throws(MiniAppSdkException::class)
    suspend fun fetchMiniAppManifest(appId: String, versionId: String): MiniAppManifest {
        return if (versionId.isEmpty()) throw MiniAppSdkException("Provided Mini App Version ID is invalid.")
        else {
            // every version should have it's own manifest information or null
            val cachedLatestManifest = manifestApiCache.readManifest(appId, versionId)
            if (cachedLatestManifest != null) cachedLatestManifest
            else {
                val apiResponse = apiClient.fetchMiniAppManifest(appId, versionId)
                val latestManifest = prepareMiniAppManifest(apiResponse)
                manifestApiCache.storeManifest(appId, versionId, latestManifest)
                latestManifest
            }
        }
    }

    @VisibleForTesting
    fun prepareMiniAppManifest(metadataEntity: MetadataEntity): MiniAppManifest {
        val requiredPermissions = listOfPermissions(metadataEntity.metadata?.requiredPermissions ?: emptyList())
        val optionalPermissions = listOfPermissions(metadataEntity.metadata?.optionalPermissions ?: emptyList())
        val customMetadata = metadataEntity.metadata?.customMetaData ?: emptyMap()
        val accessTokenPermission = metadataEntity.metadata?.accessTokenPermissions ?: emptyList()

        return MiniAppManifest(requiredPermissions, optionalPermissions, accessTokenPermission, customMetadata)
    }

    @VisibleForTesting
    fun listOfPermissions(
        permissions: List<MetadataPermissionObj>
    ): List<Pair<MiniAppCustomPermissionType, String>> {
        val pairs = ArrayList<Pair<MiniAppCustomPermissionType, String>>()
        permissions.forEach {
            val permissionType = MiniAppCustomPermissionType.getValue(it.name ?: "")
            if (permissionType.type != MiniAppCustomPermissionType.UNKNOWN.type)
                pairs.add(Pair(permissionType, it.reason ?: ""))
        }
        return pairs
    }

    @SuppressWarnings("LongMethod")
    private suspend fun downloadMiniApp(
        miniAppInfo: MiniAppInfo,
        manifest: ManifestEntity
    ): String {
        val appId = miniAppInfo.id
        val versionId = miniAppInfo.version.versionId
        val baseSavePath = storage.getMiniAppVersionPath(appId, versionId)
        when {
            isManifestValid(manifest) -> {
                for (file in manifest.files) {
                    val response = apiClient.downloadFile(file)
                    storage.saveFile(file, baseSavePath, response.byteStream())
                }
                if (!apiClient.isPreviewMode) {
                    withContext(coroutineDispatcher) {
                        launch(Job()) { storage.removeVersions(appId, versionId) }
                    }
                }
                return baseSavePath
            }
            // If backend functions correctly, this should never happen
            else -> throw sdkExceptionForInternalServerError()
        }
    }

    fun getDownloadedMiniAppList(): List<MiniAppInfo> = miniAppStatus.getDownloadedMiniAppList()

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
