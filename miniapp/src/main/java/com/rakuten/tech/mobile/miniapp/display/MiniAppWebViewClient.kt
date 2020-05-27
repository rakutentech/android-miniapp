package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceError
import androidx.annotation.VisibleForTesting
import androidx.webkit.WebViewAssetLoader
import java.io.BufferedReader

internal class MiniAppWebViewClient(
    context: Context,
    @VisibleForTesting internal val loader: WebViewAssetLoader,
    private val customDomain: String,
    private val customScheme: String
) : WebViewClient() {

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    @VisibleForTesting
    internal val bridgeJs = try {
        val inputStream = context.assets.open("bridge.js")
        inputStream.bufferedReader().use(BufferedReader::readText)
    } catch (e: Exception) {
        null
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        if (request.url.toString().contains("ico"))
            onLoadResource(view, request.url.toString())
        return loader.shouldInterceptRequest(request.url)
    }

    override fun onPageStarted(webView: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(webView, url, favicon)
        webView.evaluateJavascript(bridgeJs) {}
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
