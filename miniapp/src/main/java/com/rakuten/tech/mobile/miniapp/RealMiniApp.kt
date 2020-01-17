package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.api.MiniAppHttpException
import com.rakuten.tech.mobile.miniapp.display.Displayer

@Suppress("TodoComment", "NotImplementedDeclaration")
internal class RealMiniApp(
    val miniAppDownloader: MiniAppDownloader,
    val displayer: Displayer,
    val miniAppLister: MiniAppLister
) : MiniApp() {

    override suspend fun listMiniApp(
        success: (List<MiniAppInfo>) -> Unit,
        error: (MiniAppHttpException) -> Unit
    ) {
        lister.fetchMiniAppList()
    }

    override suspend fun create(
        appId: String,
        success: (MiniAppView) -> Unit,
        error: (Exception) -> Unit
    ) {
        TODO("not implemented")
    }
}
