package com.rakuten.tech.mobile.miniapp

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.MiniAppManifestCache

@Suppress("TooManyFunctions", "LongMethod", "ExpressionBodySyntax")
internal class RealMiniApp(
    private val apiClientRepository: ApiClientRepository,
    private val miniAppDownloader: MiniAppDownloader,
    private val displayer: Displayer,
    private val miniAppInfoFetcher: MiniAppInfoFetcher,
    initCustomPermissionCache: () -> MiniAppCustomPermissionCache,
    initManifestCache: () -> MiniAppManifestCache
) : MiniApp() {

    private val miniAppCustomPermissionCache: MiniAppCustomPermissionCache by lazy { initCustomPermissionCache() }
    private val miniAppManifestCache: MiniAppManifestCache by lazy { initManifestCache() }

    @VisibleForTesting
    var temporaryManifest: MiniAppManifest? = null

    override suspend fun listMiniApp(): List<MiniAppInfo> = miniAppInfoFetcher.fetchMiniAppList()

    override suspend fun fetchInfo(appId: String): MiniAppInfo = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppInfoFetcher.getInfo(appId)
    }

    override fun getCustomPermissions(
        miniAppId: String
    ): MiniAppCustomPermission {
        // return only the permissions listed in the Mini App's manifest.
        val manifestPermissions = miniAppManifestCache.getCachedAllPermissions(miniAppId)
        return MiniAppCustomPermission(miniAppId, manifestPermissions)
    }

    override fun setCustomPermissions(miniAppCustomPermission: MiniAppCustomPermission) {
        // store only the permissions listed in the Mini App's manifest.
        val miniAppId = miniAppCustomPermission.miniAppId
        val manifestPermissions =
            arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        if (temporaryManifest == null) {
            miniAppManifestCache.getCachedAllPermissions(miniAppId).forEach { (first) ->
                miniAppCustomPermission.pairValues.find {
                    it.first == first
                }?.let { manifestPermissions.add(it) }
            }
        } else {
            temporaryManifest?.requiredPermissions?.forEach { (first) ->
                miniAppCustomPermission.pairValues.find {
                    it.first == first
                }?.let { manifestPermissions.add(it) }
            }
            temporaryManifest?.optionalPermissions?.forEach { (first) ->
                miniAppCustomPermission.pairValues.find {
                    it.first == first
                }?.let { manifestPermissions.add(it) }
            }
        }

        val permissionsToStore = MiniAppCustomPermission(miniAppCustomPermission.miniAppId, manifestPermissions)
        miniAppCustomPermissionCache.storePermissions(permissionsToStore)
    }

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
        queryParams: String
    ): MiniAppDisplay = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        miniAppManifestCache.isRequiredPermissionDenied(
            appId, temporaryManifest
        ) -> throw MiniAppSdkException(ERR_REQUIRED_PERMISSION_DENIED)
        else -> {
            val (basePath, miniAppInfo) = miniAppDownloader.getMiniApp(appId)
            displayer.createMiniAppDisplay(
                basePath,
                miniAppInfo,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppCustomPermissionCache,
                queryParams
            )
        }
    }

    override suspend fun create(
        appInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        queryParams: String
    ): MiniAppDisplay = when {
        appInfo.id.isBlank() -> throw sdkExceptionForInvalidArguments()
        miniAppManifestCache.isRequiredPermissionDenied(
            appInfo.id, temporaryManifest
        ) -> throw MiniAppSdkException(ERR_REQUIRED_PERMISSION_DENIED)
        else -> {
            val (basePath, miniAppInfo) = miniAppDownloader.getMiniApp(appInfo)
            displayer.createMiniAppDisplay(
                basePath,
                miniAppInfo,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppCustomPermissionCache,
                queryParams
            )
        }
    }

    override suspend fun createWithUrl(
        appUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        queryParams: String
    ): MiniAppDisplay = when {
        appUrl.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            miniAppDownloader.validateHttpAppUrl(appUrl)
            displayer.createMiniAppDisplay(
                appUrl,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppCustomPermissionCache,
                queryParams
            )
        }
    }

    override suspend fun getMiniAppManifest(appId: String, versionId: String): MiniAppManifest {
        temporaryManifest = miniAppDownloader.fetchMiniAppManifest(appId, versionId)
        return temporaryManifest as MiniAppManifest
    }

    override fun getDownloadedManifest(appId: String): MiniAppManifest? {
        return miniAppManifestCache.readMiniAppManifest(appId)
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
    }

    @VisibleForTesting
    internal fun createApiClient(newConfig: MiniAppSdkConfig) = ApiClient(
        baseUrl = newConfig.baseUrl,
        rasProjectId = newConfig.rasProjectId,
        subscriptionKey = newConfig.subscriptionKey,
        isPreviewMode = newConfig.isPreviewMode
    )

    private companion object {
        const val ERR_REQUIRED_PERMISSION_DENIED = "Required permissions are not granted yet for this miniapp."
    }
}
