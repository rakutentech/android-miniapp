package com.rakuten.tech.mobile.miniapp.js

import com.rakuten.tech.mobile.miniapp.MiniAppSdkException
import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage.NO_IMPL

interface ExternalWebviewDispatcher {
    /**
     * Get external webview close info from host app.
     */
    fun onExternalWebViewClose(
        onSuccess: (info: String) -> Unit,
        onError: (infoError: String) -> Unit
    ) {
        throw MiniAppSdkException(NO_IMPL)

    }
}
