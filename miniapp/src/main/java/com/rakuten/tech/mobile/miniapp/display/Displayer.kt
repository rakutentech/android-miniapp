package com.rakuten.tech.mobile.miniapp.display

import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache

@Suppress("LongParameterList")
internal class Displayer(private val hostAppUserAgentInfo: String) {

    fun createMiniAppDisplay(
        basePath: String,
        miniAppInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        queryParams: String
    ): MiniAppDisplay = RealMiniAppDisplay(
        basePath = basePath,
        miniAppInfo = miniAppInfo,
        miniAppMessageBridge = miniAppMessageBridge,
        miniAppNavigator = miniAppNavigator,
        hostAppUserAgentInfo = hostAppUserAgentInfo,
        miniAppCustomPermissionCache = miniAppCustomPermissionCache,
        queryParams = queryParams
    )

    fun createMiniAppDisplay(
        appUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        queryParams: String
    ): MiniAppDisplay = RealMiniAppDisplay(
        appUrl = appUrl,
        miniAppMessageBridge = miniAppMessageBridge,
        miniAppNavigator = miniAppNavigator,
        hostAppUserAgentInfo = hostAppUserAgentInfo,
        miniAppCustomPermissionCache = miniAppCustomPermissionCache,
        queryParams = queryParams
    )
}
