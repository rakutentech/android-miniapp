package com.rakuten.tech.mobile.miniapp.view

import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.sdkExceptionForInvalidArguments

/**
 * This represents the configuration settings for the Mini App.
 * @property miniAppSdkConfig configuration for Mini App SDK.
 * @param miniAppMessageBridge the interface for communicating between host app & mini app.
 * @param miniAppNavigator allow host app to handle specific urls such as external link.
 * @param miniAppFileChooser allow host app to get the file path while choosing file inside the webview.
 * @param queryParams the parameters will be appended with the miniapp url scheme.
 */
data class MiniAppConfig(
    val miniAppSdkConfig: MiniAppSdkConfig?,
    val miniAppNavigator: MiniAppNavigator?,
    val miniAppFileChooser: MiniAppFileChooser?,
    val miniAppMessageBridge: MiniAppMessageBridge,
    val queryParams: String = ""
) {

    init {
        when {
            ((miniAppNavigator == null) || (miniAppFileChooser == null)) ->
                throw sdkExceptionForInvalidArguments("MiniAppConfig with invalid parameters")
        }
    }
}
