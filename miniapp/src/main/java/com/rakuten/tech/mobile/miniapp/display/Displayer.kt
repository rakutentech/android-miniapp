package com.rakuten.tech.mobile.miniapp.display

import com.rakuten.tech.mobile.miniapp.MiniAppView

internal class Displayer {

    suspend fun getViewForMiniApp(basePath: String): MiniAppView = RealMiniAppView(basePath)
}
