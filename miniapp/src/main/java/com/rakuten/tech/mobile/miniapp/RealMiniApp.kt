package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.display.Displayer

internal class RealMiniApp(
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
        versionId: String
    ): MiniAppDisplay = when {
        appId.isBlank() || versionId.isBlank() -> throw sdkExceptionForInvalidArguments()
        else -> {
            val basePath = miniAppDownloader.getMiniApp(
                appId = appId,
                versionId = versionId
            )
            displayer.createMiniAppDisplay(basePath, appId)
        }
    }

    override fun updateConfiguration(settings: MiniAppSdkConfig) {
        miniAppDownloader.setApiClient(
            ApiClient(
                baseUrl = settings.baseUrl,
                rasAppId = settings.rasAppId,
                subscriptionKey = settings.subscriptionKey,
                hostAppVersionId = settings.hostAppVersionId
            )
        )
    }
}
