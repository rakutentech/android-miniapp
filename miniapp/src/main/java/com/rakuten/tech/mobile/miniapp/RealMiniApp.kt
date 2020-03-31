package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.display.Displayer

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
        info: MiniAppInfo
    ): MiniAppDisplay = when {
        info.id.isBlank() || info.version.versionId.isBlank() ->
            throw sdkExceptionForInvalidArguments()
        else -> {
            val basePath = miniAppDownloader.getMiniApp(
                appId = info.id,
                versionId = info.version.versionId
            )
            displayer.createMiniAppDisplay(basePath, info.id)
        }
    }

    override fun updateConfiguration(newConfig: MiniAppSdkConfig) {
        var nextApiClient = apiClientRepository.getApiClientFor(newConfig.key)
        if (nextApiClient == null) {
            nextApiClient = ApiClient(
                baseUrl = newConfig.baseUrl,
                rasAppId = newConfig.rasAppId,
                subscriptionKey = newConfig.subscriptionKey,
                hostAppVersionId = newConfig.hostAppVersionId
            )
            apiClientRepository.registerApiClient(newConfig.key, nextApiClient)
        }

        nextApiClient.also {
            miniAppDownloader.updateApiClient(it)
            miniAppInfoFetcher.updateApiClient(it)
        }
    }
}
