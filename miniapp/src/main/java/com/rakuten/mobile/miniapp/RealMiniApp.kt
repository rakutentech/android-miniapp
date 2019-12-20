package com.rakuten.mobile.miniapp

@Suppress("TodoComment")
internal class RealMiniApp: MiniApp{

    override suspend fun list(success: (List<MiniAppInfo>) -> Unit, error: (Exception) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun create(
        appId: String,
        success: (MiniAppView) -> Unit,
        error: (Exception) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun createFromQrCode(
        qrCode: String,
        success: (MiniAppView) -> Unit,
        error: (Exception) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
