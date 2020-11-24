package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppScheme
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import java.util.*

internal class MiniAppHttpWebView(
    context: Context,
    miniAppTitle: String,
    val appUrl: String,
    miniAppMessageBridge: MiniAppMessageBridge,
    miniAppNavigator: MiniAppNavigator?,
    hostAppUserAgentInfo: String,
    miniAppWebChromeClient: MiniAppWebChromeClient = MiniAppWebChromeClient(context, miniAppTitle),
    miniAppCustomPermissionCache: MiniAppCustomPermissionCache
) : MiniAppWebView(
    context,
    "",
    MiniAppInfo.empty(),
    miniAppMessageBridge,
    miniAppNavigator,
    hostAppUserAgentInfo,
    miniAppWebChromeClient,
    miniAppCustomPermissionCache
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
}
