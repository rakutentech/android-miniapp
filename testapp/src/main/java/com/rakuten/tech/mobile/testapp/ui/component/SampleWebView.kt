package com.rakuten.tech.mobile.testapp.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView

@SuppressLint("SetJavaScriptEnabled")
class SampleWebView(context: Context, url: String): WebView(context) {

    init {
        settings.javaScriptEnabled = true
        loadUrl(url)
    }
}
