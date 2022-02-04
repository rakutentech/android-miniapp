package com.rakuten.tech.mobile.miniapp.ads

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.gms.ads.h5.H5AdsWebViewClient

/**
 * The class for providing functions related to H5AdsWebViewClient for using in a WebView.
 */
class MiniAppH5AdsProvider() {
    /**
     * The function to create a H5AdsWebViewClient.
     * @param context context of the webview.
     * @param webView the webview which needs to enable H5Ads.
     * @param webViewClient comment here.
     */
    fun getH5AdsWebViewClient(
        context: Context,
        webView: WebView,
        webViewClient: WebViewClient
    ): H5AdsWebViewClient {
        val h5Client = H5AdsWebViewClient(context, webView)
        h5Client.delegateWebViewClient = webViewClient
        return h5Client
    }
}
