package com.rakuten.tech.mobile.miniapp

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.analytics.Actype
import com.rakuten.tech.mobile.miniapp.analytics.Etype
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.MetadataEntity
import com.rakuten.tech.mobile.miniapp.api.MetadataPermissionObj
import com.rakuten.tech.mobile.miniapp.api.ManifestEntity
import com.rakuten.tech.mobile.miniapp.api.ManifestHeader
import com.rakuten.tech.mobile.miniapp.api.UpdatableApiClient
import com.rakuten.tech.mobile.miniapp.api.ManifestApiCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.signatureverifier.SignatureVerifier
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage
import com.rakuten.tech.mobile.miniapp.storage.verifier.CachedMiniAppVerifier
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL

@Suppress("SwallowedException", "TooManyFunctions", "LargeClass", "MaxLineLength")
internal class MiniAppDownloader(
    private var context: Context,
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
        val miniAppInfo: MiniAppInfo
        try {
            miniAppInfo = apiClient.fetchInfo(appId)
        } catch (error: MiniAppTooManyRequestsError) {
            removeMiniApp(appId, "", TOO_MANY_REQUEST_ERR_LOG)
            throw MiniAppTooManyRequestsError(error.message)
        }
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
        val versionPath =
            storage.getMiniAppVersionPath(miniAppInfo.id, miniAppInfo.version.versionId)

        if (!apiClient.isPreviewMode && miniAppStatus.isVersionDownloaded(
                miniAppInfo.id,
                miniAppInfo.version.versionId,
                versionPath
            )
        ) {
            return if (verifier.verify(miniAppInfo.version.versionId, File(versionPath)))
                versionPath
            else {
                removeMiniApp(
                    miniAppInfo.id,
                    miniAppInfo.version.versionId,
                    "Failed to verify the hash of the cached files. " +
                            "The files will be deleted and the Mini App re-downloaded."
                )
                null
            }
        }
        return null
    }

    internal fun removeMiniApp(appId: String, versionId: String = "", log: String) {
        Log.e(TAG, log)
        storage.removeApp(appId)
        if (versionId.isNotEmpty())
            miniAppStatus.setVersionDownloaded(appId, versionId, false)
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

    @SuppressWarnings("NestedBlockDepth")
    @Throws(MiniAppSdkException::class)
    suspend fun fetchMiniAppManifest(
        appId: String,
        versionId: String,
        languageCode: String
    ): MiniAppManifest {
        if (versionId.isEmpty()) throw MiniAppSdkException("Provided Mini App Version ID is invalid.")
        else {
            try {
                return if (apiClient.isPreviewMode) {
                    // every version should have it's own manifest information and it can be changed
                    val apiResponse = apiClient.fetchMiniAppManifest(appId, versionId, languageCode)
                    prepareMiniAppManifest(apiResponse, versionId)
                } else {
                    // every version should have it's own manifest information or null
                    val cachedLatestManifest =
                        manifestApiCache.readManifest(appId, versionId, languageCode)
                    if (cachedLatestManifest != null) cachedLatestManifest
                    else {
                        val apiResponse =
                            apiClient.fetchMiniAppManifest(appId, versionId, languageCode)
                        val latestManifest = prepareMiniAppManifest(apiResponse, versionId)
                        manifestApiCache.storeManifest(
                            appId,
                            versionId,
                            languageCode,
                            latestManifest
                        )
                        latestManifest
                    }
                }
            } catch (error: MiniAppTooManyRequestsError) {
                removeMiniApp(appId, versionId, TOO_MANY_REQUEST_ERR_LOG)
                throw MiniAppTooManyRequestsError(error.message)
            }
        }
    }

    internal fun saveManifestForMiniAppBundle(
        appId: String,
        versionId: String,
        languageCode: String,
        manifest: MiniAppManifest
    ) {
        manifestApiCache.storeManifest(
            appId,
            versionId,
            languageCode,
            manifest
        )
    }

    suspend fun storeMiniAppBundle(
        fileName: String,
        miniAppId: String,
        versionId: String,
        completionHandler: ((success: Boolean, MiniAppSdkException?) -> Unit)?
    ) {
        try {
            val stream: InputStream = context.assets.open(fileName)
            val versionPath = storage.saveFileFromBundle(fileName, miniAppId, versionId, stream)
            verifier.storeHashAsync(versionId, File(versionPath))
            completionHandler?.let {
                it.invoke(true, null)
            }
        } catch (e: MiniAppSdkException) {
            completionHandler?.let {
                it.invoke(false, e)
            }
        } catch (e: FileNotFoundException) {
            completionHandler?.let {
                it.invoke(false, MiniAppBundleNotFoundException())
            }
        }
    }

    @VisibleForTesting
    fun prepareMiniAppManifest(metadataEntity: MetadataEntity, versionId: String): MiniAppManifest {
        val requiredPermissions =
            listOfPermissions(metadataEntity.metadata?.requiredPermissions ?: emptyList())
        val optionalPermissions =
            listOfPermissions(metadataEntity.metadata?.optionalPermissions ?: emptyList())
        val customMetadata = metadataEntity.metadata?.customMetaData ?: emptyMap()
        val accessTokenPermission = metadataEntity.metadata?.accessTokenPermissions ?: emptyList()

        return MiniAppManifest(
            requiredPermissions, optionalPermissions,
            accessTokenPermission, customMetadata, versionId
        )
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

    @SuppressWarnings("LongMethod", "NestedBlockDepth", "ComplexMethod", "ThrowsCount")
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
                    try {
                        checkSignatureValidation(file, versionId, manifest, miniAppInfo, appId)
                        storage.saveFile(
                            file,
                            baseSavePath,
                            apiClient.downloadFile(file).byteStream()
                        )
                    } catch (error: MiniAppTooManyRequestsError) {
                        removeMiniApp(appId, versionId, TOO_MANY_REQUEST_ERR_LOG)
                        throw MiniAppTooManyRequestsError(error.message)
                    }
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

    @SuppressWarnings("LongMethod", "NestedBlockDepth", "ComplexMethod", "ThrowsCount")
    internal suspend fun downloadMiniApp(
        appId: String,
        versionId: String,
    ): String {
        val manifest = fetchManifest(appId, versionId)
        val baseSavePath = storage.getMiniAppVersionPath(appId, versionId)
        when {
            doesManifestFileExist(manifest.first) -> {
                for (file in manifest.first.files) {
                    try {
                        checkSignatureValidation(
                            file = file,
                            versionId = versionId,
                            manifest = manifest,
                            appId = appId
                        )
                        storage.saveFile(
                            file,
                            baseSavePath,
                            apiClient.downloadFile(file).byteStream()
                        )
                    } catch (error: MiniAppTooManyRequestsError) {
                        removeMiniApp(appId, versionId, TOO_MANY_REQUEST_ERR_LOG)
                        throw MiniAppTooManyRequestsError(error.message)
                    }
                }
                return baseSavePath
            }
            // If backend functions correctly, this should never happen
            else -> throw sdkExceptionForInternalServerError()
        }
    }

    @VisibleForTesting
    internal fun isMiniAppCacheAvailable(appId: String, versionId: String): Boolean {
        return storage.isMiniAppAvailable(appId, versionId)
    }

    @Suppress("LongMethod")
    private suspend fun checkSignatureValidation(
        file: String,
        versionId: String,
        manifest: Pair<ManifestEntity, ManifestHeader>,
        miniAppInfo: MiniAppInfo? = null,
        appId: String
    ) {
        if (isSignatureValid(apiClient.downloadFile(file).byteStream(), versionId, manifest)) {
            if (miniAppInfo != null) {
                miniAppAnalytics.sendAnalytics(
                    eType = Etype.CLICK,
                    actype = Actype.SIGNATURE_VALIDATION_SUCCESS,
                    miniAppInfo = miniAppInfo
                )
            } else {
                miniAppAnalytics.sendAnalytics(
                    eType = Etype.CLICK,
                    actype = Actype.SIGNATURE_VALIDATION_SUCCESS,
                    appId = appId,
                    versionId = versionId
                )
            }
        } else {
            if (miniAppInfo != null) {
                miniAppAnalytics.sendAnalytics(
                    eType = Etype.CLICK,
                    actype = Actype.SIGNATURE_VALIDATION_FAIL,
                    miniAppInfo = miniAppInfo
                )
            } else {
                miniAppAnalytics.sendAnalytics(
                    eType = Etype.CLICK,
                    actype = Actype.SIGNATURE_VALIDATION_FAIL,
                    appId = appId,
                    versionId = versionId
                )
            }
            if (requireSignatureVerification) {
                removeMiniApp(
                    appId, versionId, "$SIGNATURE_VERIFICATION_ERR " +
                            "The files will be deleted."
                )
                throw MiniAppVerificationException(SIGNATURE_VERIFICATION_ERR)
            }
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

    @Suppress("SENSELESS_COMPARISON")
    @VisibleForTesting
    internal fun doesManifestFileExist(manifest: ManifestEntity) =
        manifest != null && manifest.files != null && manifest.files.isNotEmpty()

    override fun updateApiClient(apiClient: ApiClient) {
        this.apiClient = apiClient
    }

    @SuppressWarnings("FunctionMaxLength")
    internal fun updateRequireSignatureVerification(isRequired: Boolean) {
        this.requireSignatureVerification = isRequired
    }

    fun getCachedMiniApp(appId: String): Pair<String, MiniAppInfo> {
        val miniAppInfo = miniAppStatus.getDownloadedMiniApp(appId)
        return if (!apiClient.isPreviewMode && miniAppInfo != null) onGetCachedMiniApp(miniAppInfo)
        else throw MiniAppNotFoundException(MINIAPP_NOT_FOUND_OR_CORRUPTED)
    }

    fun getCachedMiniApp(appInfo: MiniAppInfo): Pair<String, MiniAppInfo> {
        return if (!apiClient.isPreviewMode && miniAppStatus.isVersionDownloaded(
                appId = appInfo.id,
                versionId = appInfo.version.versionId,
                versionPath = storage.getMiniAppVersionPath(appInfo.id, appInfo.version.versionId)
            )
        ) onGetCachedMiniApp(appInfo) else throw MiniAppNotFoundException(
            MINIAPP_NOT_FOUND_OR_CORRUPTED
        )
    }

    @VisibleForTesting
    internal fun onGetCachedMiniApp(appInfo: MiniAppInfo): Pair<String, MiniAppInfo> {
        return Pair(
            storage.getMiniAppVersionPath(appId = appInfo.id, appInfo.version.versionId),
            appInfo
        )
    }

    companion object {
        private const val TAG = "MiniAppDownloader"
        private const val SIGNATURE_VERIFICATION_ERR =
            "Failed to verify the signature of MiniApp's zip."
        internal const val MINIAPP_NOT_FOUND_OR_CORRUPTED =
            "Mini app is not downloaded properly or corrupted"
        internal const val TOO_MANY_REQUEST_ERR_LOG =
            "The files will be deleted for too many requests error."
    }
}
