package com.rakuten.tech.mobile.miniapp.display

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge

@SuppressLint("SetJavaScriptEnabled")
internal class RealMiniAppDisplay(
    val basePath: String,
    val appId: String,
    val miniAppMessageBridge: MiniAppMessageBridge
) : MiniAppDisplay {

    private var miniAppWebView: MiniAppWebView? = null

    override fun getMiniAppView(activityContext: Context): View? =
        if (activityContext is Activity || activityContext is ActivityCompat) {
            (miniAppWebView ?: MiniAppWebView(
                context = activityContext,
                basePath = basePath,
                appId = appId,
                miniAppMessageBridge = miniAppMessageBridge
            )).rootView
        } else null

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun destroyView() {
        miniAppWebView?.destroyView()
        miniAppWebView = null
    }
}
