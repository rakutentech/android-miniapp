package com.rakuten.tech.mobile.miniapp.js

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.NO_IMPL

/**
 * A class to provide the interfaces for send callback when external webview is being closed.
 */
interface ExternalWebviewDispatcher {
    /**
     * Get external webview close info from host app.
     */
    fun onExternalWebViewClose(
        isClosed: (message: String) -> Unit,
        onError: (error: String) -> Unit
    ) {
        throw MiniAppSdkException(NO_IMPL)
    }
}
