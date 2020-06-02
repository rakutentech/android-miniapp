package com.rakuten.tech.mobile.miniapp.display

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.sdkExceptionForNoActivityContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
internal class RealMiniAppDisplay(
    val context: Context,
    val basePath: String,
    val appId: String,
    val miniAppMessageBridge: MiniAppMessageBridge
) : MiniAppDisplay {

    @VisibleForTesting
    internal var miniAppWebView: MiniAppWebView? = null

    override suspend fun getMiniAppView(): View? = provideMiniAppWebView(context)

    override suspend fun getMiniAppView(activityContext: Context): View? =
        provideMiniAppWebView(activityContext)

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun destroyView() {
        miniAppWebView?.destroyView()
        miniAppWebView = null
    }

    private suspend fun provideMiniAppWebView(activityContext: Context): View? =
        if (isContextValid(activityContext)) {
            if (miniAppWebView != null)
                miniAppWebView
            else {
                withContext(Dispatchers.Main) {
                    MiniAppWebView(
                        context = activityContext,
                        basePath = basePath,
                        appId = appId,
                        miniAppMessageBridge = miniAppMessageBridge
                    )
                }
            }
        } else throw sdkExceptionForNoActivityContext()

    @VisibleForTesting
    internal fun isContextValid(activityContext: Context) =
        activityContext is Activity || activityContext is ActivityCompat
}
