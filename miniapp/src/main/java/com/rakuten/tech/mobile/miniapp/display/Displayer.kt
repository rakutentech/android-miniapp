package com.rakuten.tech.mobile.miniapp.display

import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class Displayer {

    suspend fun createMiniAppDisplay(
        basePath: String,
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge
    ): MiniAppDisplay =
        withContext(Dispatchers.Main) {
            RealMiniAppDisplay(basePath, appId, miniAppMessageBridge)
        }
}
