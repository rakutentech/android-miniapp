package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache

internal class Displayer(private val context: Context, private val hostAppUserAgentInfo: String) {

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
        miniAppMessageBridge = miniAppMessageBridge,
        miniAppNavigator = miniAppNavigator,
        hostAppUserAgentInfo = hostAppUserAgentInfo,
        miniAppCustomPermissionCache = miniAppCustomPermissionCache
    )

    fun createMiniAppDisplay(
        appUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache
    ): MiniAppDisplay = RealMiniAppDisplay(
        context = context,
        appUrl = appUrl,
        miniAppMessageBridge = miniAppMessageBridge,
        miniAppNavigator = miniAppNavigator,
        hostAppUserAgentInfo = hostAppUserAgentInfo,
        miniAppCustomPermissionCache = miniAppCustomPermissionCache
    )
}
