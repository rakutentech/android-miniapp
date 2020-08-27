package com.rakuten.tech.mobile.testapp.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

@SuppressLint("SetJavaScriptEnabled")
class SampleWebView(context: Context, url: String, sampleWebViewClient: WebViewClient): WebView(context) {

    init {
        settings.javaScriptEnabled = true
        webViewClient = sampleWebViewClient
        loadUrl(url)
    }
}

class SampleWebViewClient(
    private val finishCallback: (url: String) -> Unit,
    private val miniAppUrlSchemes: Array<String>
): WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        if (isMiniAppScheme(url))
           finishCallback(url)
        return super.shouldOverrideUrlLoading(view, request)
    }

    fun isMiniAppScheme(url: String): Boolean {
        miniAppUrlSchemes.forEach { scheme ->
            if (url.startsWith(scheme))
                return true
        }
        return false
    }
}
