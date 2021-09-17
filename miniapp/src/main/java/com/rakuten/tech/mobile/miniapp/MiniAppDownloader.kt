package com.rakuten.tech.mobile.miniapp

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.analytics.Actype
import com.rakuten.tech.mobile.miniapp.analytics.Etype
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics
import com.rakuten.tech.mobile.miniapp.api.MetadataPermissionObj
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestApiCache
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.api.ManifestHeader
import com.rakuten.tech.mobile.miniapp.api.MetadataEntity
import com.rakuten.tech.mobile.miniapp.api.UpdatableApiClient
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.signatureverifier.SignatureVerifier
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage
import com.rakuten.tech.mobile.miniapp.storage.verifier.CachedMiniAppVerifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL

@Suppress("SwallowedException", "TooManyFunctions", "LargeClass", "MaxLineLength")
internal class MiniAppDownloader(
    private var apiClient: ApiClient,
    private val miniAppAnalytics: MiniAppAnalytics,
    private var requireSignatureVerification: Boolean = false,
    initStorage: () -> MiniAppStorage,
    initStatus: () -> MiniAppStatus,
    initVerifier: () -> CachedMiniAppVerifier,
    initManifestApiCache: () -> ManifestApiCache,
    initSignatureVerifier: () -> SignatureVerifier?,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UpdatableApiClient {
    private val storage: MiniAppStorage by lazy { initStorage() }
    private val miniAppStatus: MiniAppStatus by lazy { initStatus() }
    private val verifier: CachedMiniAppVerifier by lazy { initVerifier() }
    private val manifestApiCache: ManifestApiCache by lazy { initManifestApiCache() }
    private val signatureVerifier: SignatureVerifier? by lazy { initSignatureVerifier() }

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

    @SuppressWarnings("MaximumLineLength")
    private fun retrieveDownloadedVersionPath(miniAppInfo: MiniAppInfo): String? {
        val versionPath = storage.getMiniAppVersionPath(miniAppInfo.id, miniAppInfo.version.versionId)

        if (!apiClient.isPreviewMode && miniAppStatus.isVersionDownloaded(
                miniAppInfo.id,
                miniAppInfo.version.versionId, versionPath
            )
        ) {
            return if (verifier.verify(miniAppInfo.version.versionId, File(versionPath)))
                versionPath
            else {
                removeMiniApp(miniAppInfo, "Failed to verify the hash of the cached files. " +
                        "The files will be deleted and the Mini App re-downloaded.")
                null
            }
        }
        return null
    }

    private fun removeMiniApp(miniAppInfo: MiniAppInfo, log: String) {
        Log.e(TAG, log)
        storage.removeApp(miniAppInfo.id)
        miniAppStatus.setVersionDownloaded(
                miniAppInfo.id,
                miniAppInfo.version.versionId,
                false
        )
    }

    @VisibleForTesting
    suspend fun startDownload(miniAppInfo: MiniAppInfo): String {
        val manifest = fetchManifest(miniAppInfo.id, miniAppInfo.version.versionId)
        return downloadMiniApp(miniAppInfo, Pair(manifest.first, manifest.second))
    }

    @VisibleForTesting
    suspend fun fetchManifest(
        appId: String,
        versionId: String
    ) = apiClient.fetchFileList(appId, versionId)

    @Throws(MiniAppSdkException::class)
    suspend fun fetchMiniAppManifest(appId: String, versionId: String): MiniAppManifest {
        if (versionId.isEmpty()) throw MiniAppSdkException("Provided Mini App Version ID is invalid.")
        else {
            return if (apiClient.isPreviewMode) {
                // every version should have it's own manifest information and it can be changed
                val apiResponse = apiClient.fetchMiniAppManifest(appId, versionId)
                prepareMiniAppManifest(apiResponse, versionId)
            } else {
                // every version should have it's own manifest information or null
                val cachedLatestManifest = manifestApiCache.readManifest(appId, versionId)
                if (cachedLatestManifest != null) cachedLatestManifest
                else {
                    val apiResponse = apiClient.fetchMiniAppManifest(appId, versionId)
                    val latestManifest = prepareMiniAppManifest(apiResponse, versionId)
                    manifestApiCache.storeManifest(appId, versionId, latestManifest)
                    latestManifest
                }
            }
        }
    }

    @VisibleForTesting
    fun prepareMiniAppManifest(metadataEntity: MetadataEntity, versionId: String): MiniAppManifest {
        val requiredPermissions = listOfPermissions(metadataEntity.metadata?.requiredPermissions ?: emptyList())
        val optionalPermissions = listOfPermissions(metadataEntity.metadata?.optionalPermissions ?: emptyList())
        val customMetadata = metadataEntity.metadata?.customMetaData ?: emptyMap()
        val accessTokenPermission = metadataEntity.metadata?.accessTokenPermissions ?: emptyList()

        return MiniAppManifest(requiredPermissions, optionalPermissions,
            accessTokenPermission, customMetadata, versionId)
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

    @SuppressWarnings("LongMethod", "NestedBlockDepth")
    private suspend fun downloadMiniApp(
        miniAppInfo: MiniAppInfo,
        manifest: Pair<ManifestEntity, ManifestHeader>
    ): String {
        val appId = miniAppInfo.id
        val versionId = miniAppInfo.version.versionId
        val baseSavePath = storage.getMiniAppVersionPath(appId, versionId)
        when {
            doesManifestFileExist(manifest.first) -> {
                for (file in manifest.first.files) {
                    if (isSignatureValid(apiClient.downloadFile(file).byteStream(), versionId, manifest)) {
                        miniAppAnalytics.sendAnalytics(
                            eType = Etype.CLICK,
                            actype = Actype.SIGNATURE_VALIDATION_SUCCESS,
                            miniAppInfo = miniAppInfo
                        )
                    } else {
                        miniAppAnalytics.sendAnalytics(
                            eType = Etype.CLICK,
                            actype = Actype.SIGNATURE_VALIDATION_FAIL,
                            miniAppInfo = miniAppInfo
                        )
                        if (requireSignatureVerification) {
                            removeMiniApp(miniAppInfo, "$SIGNATURE_VERIFICATION_ERR " +
                                    "The files will be deleted.")
                            throw MiniAppVerificationException(SIGNATURE_VERIFICATION_ERR)
                        }
                    }

                    storage.saveFile(file, baseSavePath, apiClient.downloadFile(file).byteStream())
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

    private suspend fun isSignatureValid(
        inputStream: InputStream,
        versionId: String,
        manifest: Pair<ManifestEntity, ManifestHeader>
    ): Boolean = signatureVerifier?.verify(
            manifest.first.publicKeyId,
            versionId,
            inputStream,
            manifest.second.signature.toString()
    ) ?: false

    fun getDownloadedMiniAppList(): List<MiniAppInfo> = miniAppStatus.getDownloadedMiniAppList()

    @VisibleForTesting
    internal fun doesManifestFileExist(manifest: ManifestEntity) = manifest.files.isNotEmpty()

    override fun updateApiClient(apiClient: ApiClient) {
        this.apiClient = apiClient
    }

    @SuppressWarnings("FunctionMaxLength")
    internal fun updateRequireSignatureVerification(isRequired: Boolean) {
        this.requireSignatureVerification = isRequired
    }

    companion object {
        private const val TAG = "MiniAppDownloader"
        private const val SIGNATURE_VERIFICATION_ERR = "Failed to verify the signature of MiniApp's zip."
    }
}
