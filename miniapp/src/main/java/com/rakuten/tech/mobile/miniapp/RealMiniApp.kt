package com.rakuten.tech.mobile.miniapp

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.MiniAppManifestCache

@Suppress("TooManyFunctions")
internal class RealMiniApp(
    private val apiClientRepository: ApiClientRepository,
    private val miniAppDownloader: MiniAppDownloader,
    private val displayer: Displayer,
    private val miniAppInfoFetcher: MiniAppInfoFetcher,
    private val miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
    private val miniAppManifestCache: MiniAppManifestCache
) : MiniApp() {

    override suspend fun listMiniApp(): List<MiniAppInfo> = miniAppInfoFetcher.fetchMiniAppList()

    override suspend fun fetchInfo(appId: String): MiniAppInfo = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppInfoFetcher.getInfo(appId)
    }

    override fun getCustomPermissions(
        miniAppId: String
    ): MiniAppCustomPermission {
        // return only the permissions listed in the Mini App's manifest.
        val manifestPermissions = getCachedRequiredPermissions(miniAppId) +
            getCachedOptionalPermissions(miniAppId)
        return MiniAppCustomPermission(miniAppId, manifestPermissions)
    }

    override fun setCustomPermissions(miniAppCustomPermission: MiniAppCustomPermission) {
        // store only the permissions listed in the Mini App's manifest.
        val manifestPermissions =
            arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        miniAppManifestCache.readMiniAppManifest(miniAppCustomPermission.miniAppId).requiredPermissions.forEach { (first) ->
            miniAppCustomPermission.pairValues.find {
                it.first == first
            }?.let { manifestPermissions.add(it) }
        }

        miniAppManifestCache.readMiniAppManifest(miniAppCustomPermission.miniAppId).optionalPermissions.forEach { (first) ->
            miniAppCustomPermission.pairValues.find {
                it.first == first
            }?.let { manifestPermissions.add(it) }
        }

        val manifestCustomPermission = MiniAppCustomPermission(miniAppCustomPermission.miniAppId, manifestPermissions)
        miniAppCustomPermissionCache.storePermissions(manifestCustomPermission)
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
        isRequiredPermissionDenied(
            appId
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
        isRequiredPermissionDenied(
            appInfo.id
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
        // store manifest value in cache after fetching from api
        val manifest = miniAppDownloader.fetchMiniAppManifest(appId, versionId)
        miniAppManifestCache.storeMiniAppManifest(appId, manifest)
        return manifest
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

    private fun isRequiredPermissionDenied(appId: String): Boolean {
        getCachedRequiredPermissions(appId).find {
            it.second != MiniAppCustomPermissionResult.ALLOWED
        }?.let { return true }
        return false
    }

    private fun getCachedRequiredPermissions(
        appId: String
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val requiredPermissions =
            arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        val manifest = miniAppManifestCache.readMiniAppManifest(appId)
        // TODO: check if it throws error for empty version id
        manifest.requiredPermissions.forEach { (first) ->
            miniAppCustomPermissionCache.readPermissions(appId).pairValues.find {
                it.first == first
            }?.let { requiredPermissions.add(it) }
        }

        return requiredPermissions
    }

    private fun getCachedOptionalPermissions(
        appId: String
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val optionalPermissions =
            arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        // TODO: check if it throws error for empty version id
        val manifest = miniAppManifestCache.readMiniAppManifest(appId)
        manifest.optionalPermissions.forEach { (first) ->
            miniAppCustomPermissionCache.readPermissions(appId).pairValues.find {
                it.first == first
            }?.let { optionalPermissions.add(it) }
        }

        return optionalPermissions
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
