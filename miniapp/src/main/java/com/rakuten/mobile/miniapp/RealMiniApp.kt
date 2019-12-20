package com.rakuten.mobile.miniapp

import com.rakuten.mobile.miniapp.display.Displayer
import com.rakuten.mobile.miniapp.miniapp.MiniAppDownloader
import com.rakuten.mobile.miniapp.miniapp.MiniAppLister

@Suppress("TodoComment")
internal class RealMiniApp(
    val qrCodeParser: QrCodeParser,
    val miniAppDownloader: MiniAppDownloader,
    val displayer: Displayer,
    val miniAppLister: MiniAppLister
) : MiniApp {

    override suspend fun listMiniApp(
        success: (List<MiniAppInfo>) -> Unit,
        error: (Exception) -> Unit
    ) {
        TODO("not implemented")
    }

    override suspend fun create(
        appId: String,
        success: (MiniAppView) -> Unit,
        error: (Exception) -> Unit
    ) {
        TODO("not implemented")
    }

    override suspend fun createFromQrCode(
        qrCode: String,
        success: (MiniAppView) -> Unit,
        error: (Exception) -> Unit
    ) {
        TODO("not implemented")
    }
}
