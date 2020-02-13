package com.rakuten.tech.mobile.miniapp.display

import com.rakuten.tech.mobile.miniapp.MiniAppDisplay

internal class Displayer {

    suspend fun createMiniAppDisplay(basePath: String): MiniAppDisplay =
        RealMiniAppDisplay(basePath)
}
