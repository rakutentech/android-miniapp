package com.rakuten.tech.mobile.miniapp.display

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.webkit.WebView
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.sdkExceptionForNoActivityContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
internal class RealMiniAppDisplay(
    val context: Context,
    val basePath: String,
    val miniAppInfo: MiniAppInfo,
    val miniAppMessageBridge: MiniAppMessageBridge,
    val hostAppUserAgentInfo: String
) : MiniAppDisplay {

    @VisibleForTesting
    internal var miniAppWebView: MiniAppWebView? = null

    // Returns the view only when context type is legit else throw back error
    // Activity context needs to be used here, to prevent issues, where some native elements are
    // not rendered successfully in the mini app e.g. select tags
    override suspend fun getMiniAppView(activityContext: Context): View? =
        if (isContextValid(activityContext)) {
            provideMiniAppWebView(activityContext)
        } else throw sdkExceptionForNoActivityContext()

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun destroyView() {
        miniAppWebView?.destroyView()
        miniAppWebView = null
    }

    override fun navigateBackward(): Boolean = if (miniAppWebView != null) {
        val webView = (miniAppWebView as WebView)
        when {
            webView.canGoBack() -> {
                webView.goBack()
                true
            }
            else -> false
        }
    } else false

    override fun navigateForward(): Boolean = if (miniAppWebView != null) {
        val webView = (miniAppWebView as WebView)
        when {
            webView.canGoForward() -> {
                webView.goForward()
                true
            }
            else -> false
        }
    } else false

    private suspend fun provideMiniAppWebView(context: Context): MiniAppWebView =
        miniAppWebView ?: withContext(Dispatchers.Main) {
            miniAppWebView = MiniAppWebView(
                context = context,
                basePath = basePath,
                miniAppInfo = miniAppInfo,
                miniAppMessageBridge = miniAppMessageBridge,
                hostAppUserAgentInfo = hostAppUserAgentInfo
            )
            miniAppWebView!!
        }

    @VisibleForTesting
    internal fun isContextValid(activityContext: Context) =
        activityContext is Activity || activityContext is ActivityCompat
}
