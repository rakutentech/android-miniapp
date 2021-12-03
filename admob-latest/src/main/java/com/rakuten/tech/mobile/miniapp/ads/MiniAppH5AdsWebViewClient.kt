package com.rakuten.tech.mobile.miniapp.ads

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.gms.ads.h5.H5AdsWebViewClient

internal class MiniAppH5AdsWebViewClient(private val context: Context) {
    fun getH5AdsWebViewClient(miniAppWebview: WebView, miniAppClient: WebViewClient): H5AdsWebViewClient {
        val h5Client = H5AdsWebViewClient(context, miniAppWebview)
        h5Client.delegateWebViewClient = miniAppClient
        return h5Client
    }
}
