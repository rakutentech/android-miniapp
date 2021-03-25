package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppScheme
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache

internal class MiniAppHttpWebView(
    context: Context,
    miniAppInfo: MiniAppInfo,
    val appUrl: String,
    miniAppMessageBridge: MiniAppMessageBridge,
    miniAppNavigator: MiniAppNavigator?,
    miniAppFileChooser: MiniAppFileChooser?,
    hostAppUserAgentInfo: String,
    miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
    downloadedManifestCache: DownloadedManifestCache,
    miniAppWebChromeClient: MiniAppWebChromeClient = MiniAppWebChromeClient(
        context,
        miniAppInfo,
        miniAppCustomPermissionCache,
        miniAppFileChooser
    ),
    queryParams: String
) : MiniAppWebView(
    context,
    "",
    miniAppInfo,
    miniAppMessageBridge,
    miniAppNavigator,
    miniAppFileChooser,
    hostAppUserAgentInfo,
    miniAppCustomPermissionCache,
    downloadedManifestCache,
    miniAppWebChromeClient,
    queryParams
) {
    init {
        miniAppScheme = MiniAppScheme.schemeWithCustomUrl(appUrl)
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
