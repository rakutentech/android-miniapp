package com.rakuten.tech.mobile.miniapp

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.js.DB_NAME_PREFIX
import com.rakuten.tech.mobile.miniapp.js.MessageBridgeRatDispatcher
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.MiniAppSecureStorageDispatcher
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.CachedManifest
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import com.rakuten.tech.mobile.miniapp.storage.verifier.MiniAppManifestVerifier

@Suppress("TooManyFunctions", "LongMethod", "LargeClass")
internal class RealMiniApp(
    private val apiClientRepository: ApiClientRepository,
    private val miniAppDownloader: MiniAppDownloader,
    private val displayer: Displayer,
    private val miniAppInfoFetcher: MiniAppInfoFetcher,
    initCustomPermissionCache: () -> MiniAppCustomPermissionCache,
    initDownloadedManifestCache: () -> DownloadedManifestCache,
    initManifestVerifier: () -> MiniAppManifestVerifier,
    private var miniAppAnalytics: MiniAppAnalytics,
    private var ratDispatcher: MessageBridgeRatDispatcher,
    private var secureStorageDispatcher: MiniAppSecureStorageDispatcher,
    private val enableH5Ads: Boolean
) : MiniApp() {

    private val miniAppCustomPermissionCache: MiniAppCustomPermissionCache by lazy { initCustomPermissionCache() }
    private val downloadedManifestCache: DownloadedManifestCache by lazy { initDownloadedManifestCache() }
    private val manifestVerifier: MiniAppManifestVerifier by lazy { initManifestVerifier() }

    override suspend fun listMiniApp(): List<MiniAppInfo> = miniAppInfoFetcher.fetchMiniAppList()

    @SuppressWarnings("SwallowedException", "OptionalWhenBraces")
    override suspend fun fetchInfo(appId: String): MiniAppInfo = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            try {
                miniAppInfoFetcher.getInfo(appId)
            } catch (error: MiniAppTooManyRequestsError) {
                miniAppDownloader.removeMiniApp(
                    appId,
                    "",
                    MiniAppDownloader.TOO_MANY_REQUEST_ERR_LOG
                )
                throw MiniAppTooManyRequestsError(error.message)
            }
        }
    }

    override suspend fun getMiniAppInfoByPreviewCode(previewCode: String): PreviewMiniAppInfo = when {
        previewCode.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppInfoFetcher.getInfoByPreviewCode(previewCode)
    }

    override fun getCustomPermissions(miniAppId: String): MiniAppCustomPermission =
        miniAppCustomPermissionCache.readPermissions(miniAppId)

    override fun setCustomPermissions(miniAppCustomPermission: MiniAppCustomPermission) =
        miniAppCustomPermissionCache.storePermissions(miniAppCustomPermission)

    @Suppress("FunctionMaxLength")
    override fun listDownloadedWithCustomPermissions(): List<Pair<MiniAppInfo, MiniAppCustomPermission>> {
        return miniAppDownloader.getDownloadedMiniAppList().map {
            Pair(it, miniAppCustomPermissionCache.readPermissions(it.id))
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun clearSecureStorages(context: Context) {
        try {
            context.databaseList().forEach {
                if (it.startsWith(DB_NAME_PREFIX)) {
                    context.deleteDatabase(it)
                }
            }
        } catch (e: Exception) {
            // No callback needed. So Ignoring.
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun clearSecureStorage(context: Context, miniAppId: String): Boolean {
        var isDeleted: Boolean
        try {
            val dbName = DB_NAME_PREFIX + miniAppId
            context.deleteDatabase(dbName)
            isDeleted = true
            context.databaseList().forEach {
                if (it == dbName) {
                    isDeleted = false
                }
            }
        } catch (e: Exception) {
            // No callback needed. So Ignoring.
            isDeleted = false
        }
        return isDeleted
    }

    override suspend fun create(
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppFileChooser: MiniAppFileChooser?,
        queryParams: String,
        fromCache: Boolean
    ): MiniAppDisplay = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            val (basePath, miniAppInfo) = if (!fromCache) {
                miniAppDownloader.getMiniApp(appId)
            } else {
                miniAppDownloader.getCachedMiniApp(appId)
            }
            verifyManifest(miniAppInfo.id, miniAppInfo.version.versionId, fromCache)
            displayer.createMiniAppDisplay(
                basePath,
                miniAppInfo,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser,
                miniAppCustomPermissionCache,
                downloadedManifestCache,
                queryParams,
                miniAppAnalytics,
                ratDispatcher,
                secureStorageDispatcher,
                enableH5Ads
            )
        }
    }

    override suspend fun create(
        appInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppFileChooser: MiniAppFileChooser?,
        queryParams: String,
        fromCache: Boolean
    ): MiniAppDisplay = when {
        appInfo.id.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            val (basePath, miniAppInfo) = if (!fromCache) {
                miniAppDownloader.getMiniApp(appInfo)
            } else {
                miniAppDownloader.getCachedMiniApp(appInfo)
            }
            verifyManifest(miniAppInfo.id, miniAppInfo.version.versionId, fromCache)
            displayer.createMiniAppDisplay(
                basePath,
                miniAppInfo,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser,
                miniAppCustomPermissionCache,
                downloadedManifestCache,
                queryParams,
                miniAppAnalytics,
                ratDispatcher,
                secureStorageDispatcher,
                enableH5Ads
            )
        }
    }

    override suspend fun createWithUrl(
        appUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppFileChooser: MiniAppFileChooser?,
        queryParams: String
    ): MiniAppDisplay = when {
        appUrl.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            miniAppDownloader.validateHttpAppUrl(appUrl)
            displayer.createMiniAppDisplay(
                appUrl,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser,
                miniAppCustomPermissionCache,
                downloadedManifestCache,
                queryParams,
                miniAppAnalytics,
                ratDispatcher,
                secureStorageDispatcher,
                enableH5Ads
            )
        }
    }

    override suspend fun getMiniAppManifest(appId: String, versionId: String, languageCode: String): MiniAppManifest =
        miniAppDownloader.fetchMiniAppManifest(appId, versionId, languageCode)

    override fun getDownloadedManifest(appId: String): MiniAppManifest? =
        downloadedManifestCache.readDownloadedManifest(appId)?.miniAppManifest

    override fun updateConfiguration(newConfig: MiniAppSdkConfig, setConfigAsDefault: Boolean) {
        var nextApiClient = apiClientRepository.getApiClientFor(newConfig)
        if (nextApiClient == null) {
            nextApiClient = createApiClient(newConfig)
            if (setConfigAsDefault)
                apiClientRepository.registerApiClient(newConfig, nextApiClient)
        }

        nextApiClient.also {
            miniAppDownloader.updateApiClient(it)
            miniAppInfoFetcher.updateApiClient(it)
        }

        miniAppDownloader.updateRequireSignatureVerification(newConfig.requireSignatureVerification)

        if (setConfigAsDefault)
            miniAppAnalytics =
                MiniAppAnalytics(newConfig.rasProjectId, newConfig.miniAppAnalyticsConfigList)

        secureStorageDispatcher.updateMiniAppStorageMaxLimit(newConfig.maxStorageSizeLimitInBytes.toLong())
    }

    @VisibleForTesting
    suspend fun verifyManifest(appId: String, versionId: String, fromCache: Boolean = false) {
        val cachedManifest = downloadedManifestCache.readDownloadedManifest(appId)

        try {
            if (!fromCache)
                checkToDownloadManifest(appId, versionId, cachedManifest)
        } catch (e: MiniAppNetException) {
            Log.e("RealMiniApp", "Unable to retrieve latest manifest due to device being offline. " +
                    "Skipping manifest download.", e)
        }

        val manifestFile = downloadedManifestCache.getManifestFile(appId)
        if (cachedManifest != null && manifestVerifier.verify(appId, manifestFile)) {
            val customPermissions = miniAppCustomPermissionCache.readPermissions(appId)
            val manifestPermissions = downloadedManifestCache.getAllPermissions(customPermissions)
            miniAppCustomPermissionCache.removePermissionsNotMatching(appId, manifestPermissions)

            if (downloadedManifestCache.isRequiredPermissionDenied(customPermissions))
                throw RequiredPermissionsNotGrantedException(appId, versionId)
        } else {
            if (!fromCache) {
                checkToDownloadManifest(appId, versionId, cachedManifest)
            } else {
                throw MiniAppNotFoundException(MiniAppDownloader.MINIAPP_NOT_FOUND_OR_CORRUPTED)
            }
        }
    }

    @VisibleForTesting
    suspend fun checkToDownloadManifest(appId: String, versionId: String, cachedManifest: CachedManifest?) {
        val apiManifest = getMiniAppManifest(appId, versionId)
        val isDifferentVersion = cachedManifest?.versionId != versionId
        val isSameVerDiffApp = !isManifestEqual(apiManifest, cachedManifest?.miniAppManifest)
        if (isDifferentVersion || isSameVerDiffApp) {
            val storableManifest = CachedManifest(versionId, apiManifest)
            downloadedManifestCache.storeDownloadedManifest(appId, storableManifest)
            val manifestFile = downloadedManifestCache.getManifestFile(appId)
            manifestVerifier.storeHashAsync(appId, manifestFile)
        }
    }

    @VisibleForTesting
    fun isManifestEqual(apiManifest: MiniAppManifest?, downloadedManifest: MiniAppManifest?): Boolean {
        if (apiManifest != null && downloadedManifest != null) {
            val changedRequiredPermissions =
                    (apiManifest.requiredPermissions + downloadedManifest.requiredPermissions).groupBy { it.first.type }
                            .filter { it.value.size == 1 }
                            .flatMap { it.value }

            val changedOptionalPermissions =
                    (apiManifest.optionalPermissions + downloadedManifest.optionalPermissions).groupBy { it.first.type }
                            .filter { it.value.size == 1 }
                            .flatMap { it.value }

            return changedRequiredPermissions.isEmpty() && changedOptionalPermissions.isEmpty() &&
                    apiManifest.customMetaData == downloadedManifest.customMetaData
        }
        return false
    }

    @VisibleForTesting
    internal fun createApiClient(newConfig: MiniAppSdkConfig) = ApiClient(
        baseUrl = newConfig.baseUrl,
        rasProjectId = newConfig.rasProjectId,
        subscriptionKey = newConfig.subscriptionKey,
        isPreviewMode = newConfig.isPreviewMode,
        sslPublicKeyList = newConfig.sslPinningPublicKeyList
    )
}
