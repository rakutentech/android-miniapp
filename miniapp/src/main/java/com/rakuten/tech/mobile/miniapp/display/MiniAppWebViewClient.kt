package com.rakuten.tech.mobile.miniapp.display

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import androidx.webkit.WebViewAssetLoader

internal class MiniAppWebViewClient(
    @VisibleForTesting internal val loader: WebViewAssetLoader,
    private val customDomain: String,
    private val customScheme: String
) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        if (request.url != null && request.url.toString().startsWith(customScheme)) {
            val interceptUri = request.url.toString().replace(customScheme, customDomain).toUri()
            return loader.shouldInterceptRequest(interceptUri)
        }
        return loader.shouldInterceptRequest(request.url)
    }
}
