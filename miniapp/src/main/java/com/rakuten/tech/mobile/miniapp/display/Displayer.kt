package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache

internal class Displayer(
    private val context: Context,
    private val miniAppSdkConfig: MiniAppSdkConfig
) {

    fun createMiniAppDisplay(
        basePath: String,
        miniAppInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache
    ): MiniAppDisplay = RealMiniAppDisplay(
        context = context,
        basePath = basePath,
        miniAppInfo = miniAppInfo,
        miniAppSdkConfig = miniAppSdkConfig,
        miniAppMessageBridge = miniAppMessageBridge,
        miniAppNavigator = miniAppNavigator,
        miniAppCustomPermissionCache = miniAppCustomPermissionCache
    )
}
