package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.display.Displayer

internal class RealMiniApp(
    private val miniAppDownloader: MiniAppDownloader,
    private val displayer: Displayer,
    private val miniAppInfoFetcher: MiniAppInfoFetcher
) : MiniApp() {

    override suspend fun listMiniApp(): List<MiniAppInfo> = miniAppInfoFetcher.fetchMiniAppList()

    override suspend fun fetchInfo(appId: String) = miniAppInfoFetcher.getInfo(appId)

    override suspend fun create(
        appId: String,
        versionId: String
    ): MiniAppDisplay = when {
        appId.isBlank() || versionId.isBlank() -> throw MiniAppSdkException("Invalid arguments")
        else -> {
            val basePath = miniAppDownloader.startDownload(
                appId = appId,
                versionId = versionId
            )
            displayer.createMiniAppDisplay(basePath)
        }
    }
}
