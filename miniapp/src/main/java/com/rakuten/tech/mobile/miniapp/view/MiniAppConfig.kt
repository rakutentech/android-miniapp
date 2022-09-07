package com.rakuten.tech.mobile.miniapp.view

import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator

/**
 * This represents the configuration settings for the Mini App.
 * @param miniAppSdkConfig configuration for Mini App SDK.
 * @param miniAppMessageBridge the interface for communicating between host app & mini app.
 * @param miniAppNavigator allow host app to handle specific urls such as external link.
 * @param miniAppFileChooser allow host app to get the file path while choosing file inside the webview.
 * @param queryParams the parameters will be appended with the miniapp url scheme.
 */
data class MiniAppConfig(
    val miniAppSdkConfig: MiniAppSdkConfig,
    val miniAppMessageBridge: MiniAppMessageBridge,
    val miniAppNavigator: MiniAppNavigator?,
    val miniAppFileChooser: MiniAppFileChooser?,
    var queryParams: String = ""
)
