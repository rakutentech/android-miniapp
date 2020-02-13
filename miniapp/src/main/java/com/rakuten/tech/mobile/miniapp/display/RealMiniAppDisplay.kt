package com.rakuten.tech.mobile.miniapp.display

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay

@SuppressLint("SetJavaScriptEnabled")
internal class RealMiniAppDisplay(context: Context, val basePath: String) : MiniAppDisplay,
    WebView(context) {

    init {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
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

    override fun getMiniAppView(): View = this
}

@VisibleForTesting
internal class MiniAppWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
    }
}
