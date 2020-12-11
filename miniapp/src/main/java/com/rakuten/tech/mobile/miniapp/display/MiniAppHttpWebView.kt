package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppScheme
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import java.util.UUID

internal class MiniAppHttpWebView(
    context: Context,
    miniAppInfo: MiniAppInfo,
    val appUrl: String,
    miniAppMessageBridge: MiniAppMessageBridge,
    miniAppNavigator: MiniAppNavigator?,
    hostAppUserAgentInfo: String,
    miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
    miniAppWebChromeClient: MiniAppWebChromeClient = MiniAppWebChromeClient(
        context,
        miniAppInfo,
        miniAppCustomPermissionCache
    )
) : MiniAppWebView(
    context,
    "",
    MiniAppInfo.empty(),
    miniAppMessageBridge,
    miniAppNavigator,
    hostAppUserAgentInfo,
    miniAppCustomPermissionCache,
    miniAppWebChromeClient
) {
    init {
        miniAppScheme = MiniAppScheme.schemeWithCustomUrl(appUrl)
        miniAppId = UUID.randomUUID().toString() // some id is needed to handle custom permissions
        commonInit()
    }

    override fun getMiniAppWebViewClient(): MiniAppWebViewClient = MiniAppWebViewClient(
        context,
        null,
        miniAppNavigator!!,
        externalResultHandler,
        miniAppScheme
    )

    override fun getLoadUrl(): String = appUrl

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        miniAppCustomPermissionCache.removePermission(miniAppId)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun callOnDetached() {
        onDetachedFromWindow()
    }
}
