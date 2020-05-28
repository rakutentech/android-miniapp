package com.rakuten.tech.mobile.miniapp.display

import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge

internal class Displayer() {

    fun createMiniAppDisplay(
        basePath: String,
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge
    ): MiniAppDisplay = object : MiniAppDisplay {
        override val basePath = basePath
        override val appId = appId
        override val miniAppMessageBridge = miniAppMessageBridge
    }
}
