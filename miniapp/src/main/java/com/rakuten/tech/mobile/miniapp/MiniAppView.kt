package com.rakuten.tech.mobile.miniapp

import android.content.Context
import android.webkit.WebView

/**
 * This represents the contract by which a mini app is rendered to display.
 */
interface MiniAppView {

    /**
     * Provides the actual view for a mini app to the caller.
     * The caller must provide a valid [Context] object (used to access application assets).
     * @return [WebView] as mini app's view.
     */
    suspend fun obtainView(activityContext: Context): WebView

    /**
     * Destroys the WebView associated to the mini app.
     */
    fun destroyView(webView: WebView)
}
