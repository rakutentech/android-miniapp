package com.rakuten.tech.mobile.miniapp.ads

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.gms.ads.h5.H5AdsWebViewClient

class MiniAppH5AdsWebViewClient(private val context: Context) {
    fun getH5AdsWebViewClient(webView: WebView, webViewClient: WebViewClient): H5AdsWebViewClient {
        val h5Client = H5AdsWebViewClient(context, webView)
        h5Client.delegateWebViewClient = webViewClient
        return h5Client
    }
}
