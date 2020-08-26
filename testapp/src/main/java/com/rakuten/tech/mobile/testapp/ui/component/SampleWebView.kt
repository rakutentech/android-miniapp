package com.rakuten.tech.mobile.testapp.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.rakuten.tech.mobile.miniapp.navigator.ExternalResultHandler

@SuppressLint("SetJavaScriptEnabled")
class SampleWebView(context: Context, url: String, sampleWebViewClient: WebViewClient): WebView(context) {

    init {
        settings.javaScriptEnabled = true
        webViewClient = sampleWebViewClient
        loadUrl(url)
    }
}

class SampleWebViewClient(private val finishCallback: (url: String) -> Unit): WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        if (ExternalResultHandler.shouldClose(url))
           finishCallback(url)
        return super.shouldOverrideUrlLoading(view, request)
    }
}
