package com.rakuten.tech.mobile.miniapp

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.MiniAppPermissionType

internal class RealMiniApp(
    private val apiClientRepository: ApiClientRepository,
    private val miniAppDownloader: MiniAppDownloader,
    private val displayer: Displayer,
    private val miniAppInfoFetcher: MiniAppInfoFetcher
) : MiniApp() {

    override suspend fun listMiniApp(): List<MiniAppInfo> = miniAppInfoFetcher.fetchMiniAppList()

    override suspend fun fetchInfo(appId: String): MiniAppInfo = when {
        appId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> miniAppInfoFetcher.getInfo(appId)
    }

    @Suppress("TooGenericExceptionThrown")
    override suspend fun create(info: MiniAppInfo): MiniAppDisplay =
        executingCreate(info.id, object : MiniAppMessageBridge() {
            override fun getUniqueId(): String = throw sdkExceptionForNoMiniAppMessageBridge()

            override fun requestPermission(
                miniAppPermissionType: MiniAppPermissionType,
                callback: (isGranted: Boolean) -> Unit
            ) = throw sdkExceptionForNoMiniAppMessageBridge()
        })

    override suspend fun create(
        info: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge
    ): MiniAppDisplay = executingCreate(info.id, miniAppMessageBridge)

    override suspend fun create(
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge
    ): MiniAppDisplay = executingCreate(appId, miniAppMessageBridge)

    private suspend fun executingCreate(
        miniAppId: String,
        miniAppMessageBridge: MiniAppMessageBridge
    ): MiniAppDisplay = when {
        miniAppId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            val basePath = miniAppDownloader.getMiniApp(miniAppId)
            displayer.createMiniAppDisplay(basePath, miniAppId, miniAppMessageBridge)
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
