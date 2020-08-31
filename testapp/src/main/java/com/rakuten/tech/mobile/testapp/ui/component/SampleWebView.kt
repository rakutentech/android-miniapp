package com.rakuten.tech.mobile.testapp.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppExternalUrlLoader

@SuppressLint("SetJavaScriptEnabled")
class SampleWebView(context: Context, url: String, sampleWebViewClient: WebViewClient): WebView(context) {

    init {
        settings.javaScriptEnabled = true
        webViewClient = sampleWebViewClient
        loadUrl(url)
    }
}

class SampleWebViewClient(
    private val miniAppExternalUrlLoader: MiniAppExternalUrlLoader
): WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        return miniAppExternalUrlLoader.shouldOverrideUrlLoading(url)
    }
}
