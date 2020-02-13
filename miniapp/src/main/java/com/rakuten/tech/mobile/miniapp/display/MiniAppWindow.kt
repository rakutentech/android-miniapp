package com.rakuten.tech.mobile.miniapp.display

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting

@SuppressLint("SetJavaScriptEnabled")
internal class MiniAppWindow(context: Context, val basePath: String) : WebView(context) {

    init {
        setLayoutParams(
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        settings.javaScriptEnabled = true
        settings.allowUniversalAccessFromFileURLs = true
        webViewClient = MiniAppWebViewClient()
        loadUrl(getLoadUrl())
    }

    override fun setWebViewClient(client: WebViewClient?) {
        super.setWebViewClient(client ?: MiniAppWebViewClient())
    }

    @VisibleForTesting
    fun getLoadUrl() = "file://$basePath/index.html"
}

internal class MiniAppWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
    }
}
