package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.api.MiniAppHttpException
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.miniapp.Downloader
import com.rakuten.tech.mobile.miniapp.miniapp.Lister

@Suppress("TodoComment", "NotImplementedDeclaration")
internal class RealMiniApp(
    val downloader: Downloader,
    val displayer: Displayer,
    val lister: Lister
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
