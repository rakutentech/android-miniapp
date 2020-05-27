package com.rakuten.tech.mobile.miniapp.display

import android.webkit.*
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import androidx.webkit.WebViewAssetLoader

internal class MiniAppWebViewClient(
    @VisibleForTesting internal val loader: WebViewAssetLoader,
    private val customDomain: String,
    private val customScheme: String
) : WebViewClient() {

//    override fun shouldInterceptRequest(
//        view: WebView,
//        request: WebResourceRequest
//    ): WebResourceResponse? {
//        if (request.url != null && request.url.toString().startsWith(customScheme)) {
//            val interceptUri = request.url.toString().replace(customScheme, customDomain).toUri()
//            return loader.shouldInterceptRequest(interceptUri)
//        }
//        return loader.shouldInterceptRequest(request.url)
//    }
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? = loader.shouldInterceptRequest(request.url)

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        if (request.url != null && request.url.toString().startsWith(customScheme)) {
            view.loadUrl(request.url.toString().replace(customScheme, customDomain))
            return
        }
        super.onReceivedError(view, request, error)
    }
}
