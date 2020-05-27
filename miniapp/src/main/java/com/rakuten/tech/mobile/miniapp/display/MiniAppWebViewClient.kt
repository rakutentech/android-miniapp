package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceError
import androidx.annotation.VisibleForTesting
import androidx.webkit.WebViewAssetLoader
import java.io.BufferedReader

internal class MiniAppWebViewClientt(
    context: Context,
    @VisibleForTesting internal val loader: WebViewAssetLoader,
    private val customDomain: String,
    private val customScheme: String
) : WebViewClient() {

    @VisibleForTesting
    internal var isJsInjected = false
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private val bridgeJs = try {
        val inputStream = context.assets.open("bridge.js")
        inputStream.bufferedReader().use(BufferedReader::readText)
    } catch (e: Exception) {
        null
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? = loader.shouldInterceptRequest(request.url)

    override fun onLoadResource(webView: WebView, url: String?) {
        super.onLoadResource(webView, url)
        if (!isJsInjected) {
            webView.evaluateJavascript(bridgeJs) {}
            isJsInjected = true
        }
    }

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
