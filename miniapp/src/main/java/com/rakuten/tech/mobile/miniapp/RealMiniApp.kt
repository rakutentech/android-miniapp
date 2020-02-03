package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.display.Displayer

internal class RealMiniApp(
    val miniAppDownloader: MiniAppDownloader,
    val displayer: Displayer,
    val miniAppLister: MiniAppLister
) : MiniApp() {

    override suspend fun listMiniApp(): List<MiniAppInfo> = miniAppLister.fetchMiniAppList()

    override suspend fun create(
        miniAppInfo: MiniAppInfo
    ): MiniAppView {
        val basePath = miniAppDownloader.startDownload(
            appId = miniAppInfo.id,
            versionId = miniAppInfo.versionId
        )
        return displayer.getViewForMiniApp(basePath)
    }
}
