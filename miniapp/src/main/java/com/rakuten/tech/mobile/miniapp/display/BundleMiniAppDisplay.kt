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
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.js.NativeEventType
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.sdkExceptionForNoActivityContext
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled", "ParameterListWrapping")
internal class BundleMiniAppDisplay(
    private val basePath: String,
    val miniAppInfo: MiniAppInfo,
    val miniAppMessageBridge: MiniAppMessageBridge,
    val miniAppNavigator: MiniAppNavigator?,
    val hostAppUserAgentInfo: String,
    val miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
    val downloadedManifestCache: DownloadedManifestCache,
    val queryParams: String
) : MiniAppDisplay {

    var appUrl: String? = null
        private set
    @VisibleForTesting
    internal var miniAppWebView: BundleMiniAppWebView? = null
    @VisibleForTesting

    constructor(
        appUrl: String,
        miniAppMessageBridge: MiniAppMessageBridge,
        miniAppNavigator: MiniAppNavigator?,
        hostAppUserAgentInfo: String,
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        downloadedManifestCache: DownloadedManifestCache,
        queryParams: String
    ) : this(
        "",
        MiniAppInfo.forUrl(),
        miniAppMessageBridge,
        miniAppNavigator,
        hostAppUserAgentInfo,
        miniAppCustomPermissionCache,
        downloadedManifestCache,
        queryParams
    ) {
        this.appUrl = appUrl
    }

    // Returns the view only when context type is legit else throw back error
    // Activity context needs to be used here, to prevent issues, where some native elements are
    // not rendered successfully in the mini app e.g. select tags
    override suspend fun getMiniAppView(activityContext: Context): View? =
        if (isContextValid(activityContext)) {
            // send analytics tracking when Host App displays a mini app.
            provideMiniAppWebView(activityContext)
        } else throw sdkExceptionForNoActivityContext()

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun destroyView() {
        // send analytics tracking when mini app is closed.
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

    override fun sendJsonToMiniApp(message: String) {
        miniAppMessageBridge.dispatchNativeEvent(NativeEventType.MINIAPP_RECEIVE_JSON_INFO, message)
    }

    @Suppress("LongMethod")
    private suspend fun provideMiniAppWebView(context: Context): BundleMiniAppWebView =
        miniAppWebView ?: withContext(Dispatchers.Main) {
            miniAppWebView = BundleMiniAppWebView(
                context = context,
                basePath = basePath,
                miniAppInfo = miniAppInfo,
                miniAppMessageBridge = miniAppMessageBridge,
                miniAppNavigator = miniAppNavigator,
                hostAppUserAgentInfo = hostAppUserAgentInfo,
                miniAppCustomPermissionCache = miniAppCustomPermissionCache,
                downloadedManifestCache = downloadedManifestCache,
                queryParams = queryParams
            )
            miniAppWebView!!
        }

    @VisibleForTesting
    internal fun isContextValid(activityContext: Context) =
        activityContext is Activity || activityContext is ActivityCompat
}
