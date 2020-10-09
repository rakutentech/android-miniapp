package com.rakuten.tech.mobile.miniapp.js

import com.rakuten.tech.mobile.miniapp.display.WebViewListener

internal class MiniAppBridgeExecutor(private val webViewListener: WebViewListener) {

    /** Emit a value to mini app. **/
    fun postValue(callbackId: String, value: String) {
        webViewListener.runSuccessCallback(callbackId, value)
    }

    /** Emit an error response to mini app. **/
    fun postError(callbackId: String, errorMessage: String) {
        webViewListener.runErrorCallback(callbackId, errorMessage)
    }
}
