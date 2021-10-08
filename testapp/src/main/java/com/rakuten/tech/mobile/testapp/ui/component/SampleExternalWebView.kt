package com.rakuten.tech.mobile.testapp.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.os.Message
import android.webkit.WebChromeClient
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
        settings.setSupportMultipleWindows(true)
        webViewClient = sampleWebViewClient
        webChromeClient = SampleWebChromeClient(context)
        loadUrl(url)
    }
}

class SampleWebViewClient(private val miniAppExternalUrlLoader: MiniAppExternalUrlLoader): WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
        miniAppExternalUrlLoader.shouldOverrideUrlLoading(url ?: "")
}

class SampleWebChromeClient(val context: Context) : WebChromeClient() {

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        return if (isUserGesture) {
            // redirect to default browser when there is window.open / _blank type anchor
            (resultMsg?.obj as WebView.WebViewTransport).webView = WebView(context)
            resultMsg.sendToTarget()
            true
        } else false
    }
}
