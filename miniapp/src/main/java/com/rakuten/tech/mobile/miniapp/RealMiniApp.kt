package com.rakuten.tech.mobile.miniapp

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.analytics.Actype
import com.rakuten.tech.mobile.miniapp.analytics.Etype
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.CachedManifest
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import com.rakuten.tech.mobile.miniapp.storage.verifier.MiniAppManifestVerifier
import com.rakuten.tech.mobile.testapp.rat_wrapper.RATEvent

@Suppress("TooManyFunctions", "LongMethod")
internal class RealMiniApp(
    private val apiClientRepository: ApiClientRepository,
    private val miniAppDownloader: MiniAppDownloader,
    private val displayer: Displayer,
    private val miniAppInfoFetcher: MiniAppInfoFetcher,
    initCustomPermissionCache: () -> MiniAppCustomPermissionCache,
    initDownloadedManifestCache: () -> DownloadedManifestCache,
    initManifestVerifier: () -> MiniAppManifestVerifier,
    private var miniAppAnalytics: MiniAppAnalytics
) : MiniApp() {

    private val miniAppCustomPermissionCache: MiniAppCustomPermissionCache by lazy { initCustomPermissionCache() }
    private val downloadedManifestCache: DownloadedManifestCache by lazy { initDownloadedManifestCache() }
    private val manifestVerifier: MiniAppManifestVerifier by lazy { initManifestVerifier() }

    override suspend fun listMiniApp(): List<MiniAppInfo> = miniAppInfoFetcher.fetchMiniAppList()

    override suspend fun fetchInfo(appId: String): MiniAppInfo = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppInfoFetcher.getInfo(appId)
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

    override suspend fun create(
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppFileChooser: MiniAppFileChooser?,
        queryParams: String
    ): MiniAppDisplay = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            val (basePath, miniAppInfo) = miniAppDownloader.getMiniApp(appId)
            verifyManifest(appId, miniAppInfo.version.versionId)
            displayer.createMiniAppDisplay(
                basePath,
                miniAppInfo,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser,
                miniAppCustomPermissionCache,
                downloadedManifestCache,
                queryParams,
                miniAppAnalytics
            )
        }
    }

    override suspend fun create(
        appInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppFileChooser: MiniAppFileChooser?,
        queryParams: String
    ): MiniAppDisplay = when {
        appInfo.id.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            val (basePath, miniAppInfo) = miniAppDownloader.getMiniApp(appInfo)
            verifyManifest(appInfo.id, appInfo.version.versionId)
            displayer.createMiniAppDisplay(
                basePath,
                miniAppInfo,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppFileChooser,
                miniAppCustomPermissionCache,
                downloadedManifestCache,
                queryParams,
                miniAppAnalytics
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
                miniAppAnalytics
            )
        }
    }

    override suspend fun getMiniAppManifest(appId: String, versionId: String): MiniAppManifest =
        miniAppDownloader.fetchMiniAppManifest(appId, versionId)

    override fun getDownloadedManifest(appId: String): MiniAppManifest? =
        downloadedManifestCache.readDownloadedManifest(appId)?.miniAppManifest

    override fun sendTrackEvent(ratEvent: RATEvent) {
        miniAppAnalytics.sendAnalytics(
            eType = Etype.CLICK,
            actype = Actype.OPEN,
            miniAppInfo = null
        )
    }

    override fun updateConfiguration(newConfig: MiniAppSdkConfig) {
        var nextApiClient = apiClientRepository.getApiClientFor(newConfig.key)
        if (nextApiClient == null) {
            nextApiClient = createApiClient(newConfig)
            apiClientRepository.registerApiClient(newConfig.key, nextApiClient)
        }

        nextApiClient.also {
            miniAppDownloader.updateApiClient(it)
            miniAppInfoFetcher.updateApiClient(it)
        }

        miniAppAnalytics =
            MiniAppAnalytics(newConfig.rasProjectId, newConfig.miniAppAnalyticsConfigList)
    }

    @VisibleForTesting
    suspend fun verifyManifest(appId: String, versionId: String) {
        val cachedManifest = downloadedManifestCache.readDownloadedManifest(appId)
        checkToDownloadManifest(appId, versionId, cachedManifest)
        val manifestFile = downloadedManifestCache.getManifestFile(appId)
        if (cachedManifest != null && manifestVerifier.verify(appId, manifestFile)) {
            val customPermissions = miniAppCustomPermissionCache.readPermissions(appId)
            val manifestPermissions = downloadedManifestCache.getAllPermissions(customPermissions)
            miniAppCustomPermissionCache.removePermissionsNotMatching(appId, manifestPermissions)

            if (downloadedManifestCache.isRequiredPermissionDenied(customPermissions))
                throw RequiredPermissionsNotGrantedException(appId, versionId)
        } else checkToDownloadManifest(appId, versionId, cachedManifest)
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
        isPreviewMode = newConfig.isPreviewMode
    )
}
