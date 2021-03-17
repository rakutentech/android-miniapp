package com.rakuten.tech.mobile.miniapp.display

import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.file.MiniAppFilePicker
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache

@Suppress("LongParameterList")
internal class Displayer(private val hostAppUserAgentInfo: String) {

    fun createMiniAppDisplay(
        basePath: String,
        miniAppInfo: MiniAppInfo,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppFilePicker: MiniAppFilePicker?,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        downloadedManifestCache: DownloadedManifestCache,
        queryParams: String
    ): MiniAppDisplay = RealMiniAppDisplay(
        basePath = basePath,
        miniAppInfo = miniAppInfo,
        miniAppMessageBridge = miniAppMessageBridge,
        miniAppNavigator = miniAppNavigator,
        miniAppFilePicker = miniAppFilePicker,
        hostAppUserAgentInfo = hostAppUserAgentInfo,
        miniAppCustomPermissionCache = miniAppCustomPermissionCache,
        downloadedManifestCache = downloadedManifestCache,
        queryParams = queryParams
    )

    fun createMiniAppDisplay(
        appUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        miniAppFilePicker: MiniAppFilePicker?,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        downloadedManifestCache: DownloadedManifestCache,
        queryParams: String
    ): MiniAppDisplay = RealMiniAppDisplay(
        appUrl = appUrl,
        miniAppMessageBridge = miniAppMessageBridge,
        miniAppNavigator = miniAppNavigator,
        miniAppFilePicker = miniAppFilePicker,
        hostAppUserAgentInfo = hostAppUserAgentInfo,
        miniAppCustomPermissionCache = miniAppCustomPermissionCache,
        downloadedManifestCache = downloadedManifestCache,
        queryParams = queryParams
    )
}
