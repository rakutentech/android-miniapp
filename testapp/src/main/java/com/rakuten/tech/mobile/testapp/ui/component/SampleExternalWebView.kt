package com.rakuten.tech.mobile.testapp.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppExternalUrlLoader

@SuppressLint("SetJavaScriptEnabled")
class SampleExternalWebView(context: Context, url: String, sampleWebViewClient: WebViewClient): WebView(context) {

    init {
        settings.javaScriptEnabled = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        webViewClient = sampleWebViewClient
        loadUrl(url)
    }
}

class SampleWebViewClient(private val miniAppExternalUrlLoader: MiniAppExternalUrlLoader): WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
        miniAppExternalUrlLoader.shouldOverrideUrlLoading(url ?: "")
}
