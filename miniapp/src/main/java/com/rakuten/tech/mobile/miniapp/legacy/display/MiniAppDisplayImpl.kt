package com.rakuten.tech.mobile.miniapp.legacy.display

import android.app.Activity
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.rakuten.tech.mobile.miniapp.R
import timber.log.Timber
import java.io.File

/**
 * Displaying mini app implementation.
 */
class MiniAppDisplayImpl : MiniAppDisplayer {

    /**
     * Display mini app based on mini app ID, and version ID.
     */
    override fun displayMiniApp(miniAppId: String, versionId: String, hostActivity: Activity) {

        val indexHtmlPath = getMiniAppIndexPath(hostActivity.filesDir.path, miniAppId, versionId)
        if (File(indexHtmlPath).exists()) {
            // Display MiniApp if activity has focus.
            loadMiniApp(hostActivity, indexHtmlPath)
        } else {
            // TODO: Download app.
        }
    }

    /**
     * Loading mini app from local files.
     */
    private fun loadMiniApp(hostActivity: Activity, indexHtmlPath: String) {
        val miniAppView = hostActivity.layoutInflater.inflate(R.layout.mini_app_view, null)
        val webView = miniAppView.findViewById<WebView>(R.id.mini_app_web_view)
        webView.webViewClient = MiniAppWebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.loadUrl(indexHtmlPath)
        hostActivity.setContentView(miniAppView, hostActivity.window.attributes)
    }

    /**
     * Returns the directory of the mini app index.html.
     */
    private fun getMiniAppIndexPath(
        indexDir: String,
        miniAppId: String,
        versionId: String
    ): String = "file://$indexDir/miniapp/$miniAppId/$versionId/index.html"

    /**
     * Mini App Web Client.
     */
    private class MiniAppWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            Timber.tag("Mini_").d("onPageFinished")
            super.onPageFinished(view, url)
        }
    }
}
