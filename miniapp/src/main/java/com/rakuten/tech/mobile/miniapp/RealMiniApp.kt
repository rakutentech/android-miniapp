package com.rakuten.tech.mobile.miniapp

import android.app.Activity
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.permission.ui.MiniAppCustomPermissionWindow

@SuppressWarnings("TooManyFunctions")
internal class RealMiniApp(
    private val apiClientRepository: ApiClientRepository,
    private val miniAppDownloader: MiniAppDownloader,
    private val displayer: Displayer,
    private val miniAppInfoFetcher: MiniAppInfoFetcher,
    private val miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
    private val miniAppCustomPermissionWindow: MiniAppCustomPermissionWindow
) : MiniApp() {

    override suspend fun listMiniApp(): List<MiniAppInfo> = miniAppInfoFetcher.fetchMiniAppList()

    override suspend fun fetchInfo(appId: String): MiniAppInfo = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppInfoFetcher.getInfo(appId)
    }

    override fun getCustomPermissions(miniAppId: String): MiniAppCustomPermission =
        miniAppCustomPermissionCache.readPermissions(miniAppId)

    override fun setCustomPermissions(miniAppCustomPermission: MiniAppCustomPermission) =
        miniAppCustomPermissionCache.storePermissions(miniAppCustomPermission)

    override fun showCustomPermissionWindow(
        activity: Activity,
        miniAppId: String,
        permissionsWithDescription: List<Pair<MiniAppCustomPermissionType, String>>,
        callback: (List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>) -> Unit
    ) = miniAppCustomPermissionWindow.show(
        activity,
        miniAppId,
        permissionsWithDescription,
        callback
    )

    @Suppress("FunctionMaxLength")
    override fun listDownloadedWithCustomPermissions(): List<Pair<MiniAppInfo, MiniAppCustomPermission>> {
        return miniAppDownloader.getDownloadedMiniAppList().map {
            Pair(it, miniAppCustomPermissionCache.readPermissions(it.id))
        }
    }

    override suspend fun create(
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge
    ): MiniAppDisplay = executingCreate(appId, miniAppMessageBridge)

    override suspend fun create(
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator
    ): MiniAppDisplay = executingCreate(appId, miniAppMessageBridge, miniAppNavigator)

    private suspend fun executingCreate(
        miniAppId: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator? = null
    ): MiniAppDisplay = when {
        miniAppId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            val (basePath, miniAppInfo) = miniAppDownloader.getMiniApp(miniAppId)
            displayer.createMiniAppDisplay(
                basePath,
                miniAppInfo,
                miniAppMessageBridge,
                miniAppNavigator,
                miniAppCustomPermissionCache
            )
        }
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
        rasAppId = newConfig.rasAppId,
        subscriptionKey = newConfig.subscriptionKey,
        hostAppVersionId = newConfig.hostAppVersionId,
        isTestMode = newConfig.isTestMode
    )
}
