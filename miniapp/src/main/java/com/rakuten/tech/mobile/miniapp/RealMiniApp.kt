package com.rakuten.tech.mobile.miniapp

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge

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
            val (basePath, miniAppInfo) = miniAppDownloader.getMiniApp(miniAppId)
            displayer.createMiniAppDisplay(basePath, miniAppInfo, miniAppMessageBridge)
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
