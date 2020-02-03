package com.rakuten.tech.mobile.miniapp.display

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.rakuten.tech.mobile.miniapp.MiniAppView

internal class RealMiniAppView(
    val basePath: String
) : MiniAppView {

    @SuppressLint("SetJavaScriptEnabled")
    override suspend fun obtainView(context: Context): WebView =
        WebView(context).apply {
            setLayoutParams(
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            settings.javaScriptEnabled = true
            setWebViewClient(object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return true
                }
            })
            loadUrl("$basePath/index.html")
        }

    override fun destroyView(webView: WebView) {
        webView.apply {
            stopLoading()
            webViewClient = null
            clearHistory()
            destroy()
        }
    }
}
