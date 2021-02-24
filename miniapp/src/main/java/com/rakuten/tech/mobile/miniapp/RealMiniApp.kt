package com.rakuten.tech.mobile.miniapp

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache

@Suppress("TooManyFunctions")
internal class RealMiniApp(
    private val apiClientRepository: ApiClientRepository,
    private val miniAppDownloader: MiniAppDownloader,
    private val displayer: Displayer,
    private val miniAppInfoFetcher: MiniAppInfoFetcher,
    private val miniAppCustomPermissionCache: MiniAppCustomPermissionCache
) : MiniApp() {

    override suspend fun listMiniApp(): List<MiniAppInfo> = miniAppInfoFetcher.fetchMiniAppList()

    override suspend fun fetchInfo(appId: String): MiniAppInfo = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppInfoFetcher.getInfo(appId)
    }

    override suspend fun getCustomPermissions(
        miniAppId: String
    ): MiniAppCustomPermission {
        val permissions = getManifestCustomPermissions(miniAppId, fetchInfo(miniAppId).version.versionId)
        val miniAppCustomPermission = MiniAppCustomPermission(miniAppId, permissions)
        return miniAppCustomPermission
    }

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
        queryParams: String
    ): MiniAppDisplay = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        isRequiredPermissionDenied(
            appId,
            ""
        ) -> throw MiniAppSdkException("Required permissions are not granted yet for this miniapp.")
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
        isRequiredPermissionDenied(
            appInfo.id,
            appInfo.version.versionId
        ) -> throw MiniAppSdkException("Required permissions are not granted yet for this miniapp.")
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

    override suspend fun getMiniAppManifest(appId: String, versionId: String): MiniAppManifest =
        miniAppDownloader.fetchMiniAppManifest(appId, versionId)

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

    private suspend fun isRequiredPermissionDenied(appId: String, versionId: String): Boolean {
        getRequiredCustomPermissions(appId, versionId).find {
            it.second != MiniAppCustomPermissionResult.ALLOWED
        }?.let { return true }
        return false
    }

    private suspend fun getRequiredCustomPermissions(
        appId: String,
        versionId: String
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val requiredPermissions =
            arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        // TODO: check if it throws error for empty version id
        // TODO: fetchCachedManifest
        getMiniAppManifest(appId, versionId).requiredPermissions.forEach { (first) ->
            miniAppCustomPermissionCache.readPermissions(appId).pairValues.find {
                it.first == first
            }?.let { requiredPermissions.add(it) }
        }

        return requiredPermissions
    }

    private suspend fun getManifestCustomPermissions(
        appId: String,
        versionId: String
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val manifestPermissions = ArrayList<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()
        manifestPermissions.addAll(getRequiredCustomPermissions(appId, versionId))

        // TODO: check if it throws error for empty version id
        // TODO: fetchCachedManifest
        getMiniAppManifest(appId, versionId).optionalPermissions.forEach { (first) ->
            miniAppCustomPermissionCache.readPermissions(appId).pairValues.find {
                it.first == first
            }?.let { manifestPermissions.add(it) }
        }

        return manifestPermissions
    }

    @VisibleForTesting
    internal fun createApiClient(newConfig: MiniAppSdkConfig) = ApiClient(
        baseUrl = newConfig.baseUrl,
        rasProjectId = newConfig.rasProjectId,
        subscriptionKey = newConfig.subscriptionKey,
        isPreviewMode = newConfig.isPreviewMode
    )
}
