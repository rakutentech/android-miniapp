package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge

internal class Displayer(private val context: Context, private val hostAppInfo: String) {

    fun createMiniAppDisplay(
        basePath: String,
        appId: String,
        miniAppMessageBridge: MiniAppMessageBridge
    ): MiniAppDisplay = RealMiniAppDisplay(
        context = context,
        basePath = basePath,
        appId = appId,
        miniAppMessageBridge = miniAppMessageBridge,
        hostAppInfo = hostAppInfo
    )
}
