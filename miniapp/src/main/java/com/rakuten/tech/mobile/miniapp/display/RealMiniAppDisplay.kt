package com.rakuten.tech.mobile.miniapp.display

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
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

    override suspend fun getMiniAppView(): View = provideMiniAppWebView(context)

    override suspend fun getMiniAppView(providedContext: Context): View? =
        provideMiniAppWebView(providedContext)

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun destroyView() {
        miniAppWebView?.destroyView()
        miniAppWebView = null
    }

    private suspend fun provideMiniAppWebView(providedContext: Context) =
        withContext(Dispatchers.Main) {
            (miniAppWebView ?: MiniAppWebView(
                context = providedContext,
                basePath = basePath,
                appId = appId,
                miniAppMessageBridge = miniAppMessageBridge
            ))
    }
}
