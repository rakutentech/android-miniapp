package com.rakuten.tech.mobile.miniapp.display

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.webkit.WebViewAssetLoader
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppScheme
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.ExternalResultHandler
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import java.io.File

private const val SUB_DOMAIN_PATH = "miniapp"
private const val MINI_APP_INTERFACE = "MiniAppAndroid"

@SuppressLint("SetJavaScriptEnabled")
internal open class MiniAppWebView(
    context: Context,
    val basePath: String,
    val miniAppInfo: MiniAppInfo,
    val miniAppMessageBridge: MiniAppMessageBridge,
    var miniAppNavigator: MiniAppNavigator?,
    private val miniAppFileChooser: MiniAppFileChooser?,
    val hostAppUserAgentInfo: String,
    val miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
    val downloadedManifestCache: DownloadedManifestCache,
    val miniAppWebChromeClient: MiniAppWebChromeClient = MiniAppWebChromeClient(
        context,
        miniAppInfo,
        miniAppCustomPermissionCache,
        miniAppFileChooser
    ),
    val queryParams: String
) : WebView(context), WebViewListener {

    protected var miniAppScheme = MiniAppScheme.schemeWithAppId(miniAppInfo.id)
    protected var miniAppId = miniAppInfo.id

    @VisibleForTesting
    internal val externalResultHandler = ExternalResultHandler().apply {
        onResultChanged = { externalUrl ->
            if (externalUrl.startsWith(miniAppScheme.miniAppCustomScheme))
                loadUrl(externalUrl.replace(miniAppScheme.miniAppCustomScheme, miniAppScheme.miniAppCustomDomain))
            else if (externalUrl.startsWith(miniAppScheme.miniAppCustomDomain))
                loadUrl(externalUrl)
        }
    }

    init {
        if (this::class == MiniAppWebView::class)
            commonInit()
    }

    @Suppress("LongMethod")
    protected fun commonInit() {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )

        settings.javaScriptEnabled = true
        addJavascriptInterface(miniAppMessageBridge, MINI_APP_INTERFACE)

        miniAppMessageBridge.init(
            activity = context as Activity,
            webViewListener = this,
            customPermissionCache = miniAppCustomPermissionCache,
            downloadedManifestCache = downloadedManifestCache,
            miniAppId = miniAppId
        )

        settings.allowUniversalAccessFromFileURLs = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false

        if (hostAppUserAgentInfo.isNotEmpty())
            settings.userAgentString =
                String.format("%s %s", settings.userAgentString, hostAppUserAgentInfo)

        setupMiniAppNavigator()
        webViewClient = getMiniAppWebViewClient()
        webChromeClient = miniAppWebChromeClient

        loadUrl(getLoadUrl())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onResume()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        onPause()
        if (!(context as Activity).isDestroyed) {
            miniAppWebChromeClient.onWebViewDetach()
            miniAppMessageBridge.onWebViewDetach()
        }
    }

    fun destroyView() {
        stopLoading()
        destroy()
    }

    @VisibleForTesting
    internal fun setupMiniAppNavigator() {
        if (miniAppNavigator == null) {
            miniAppNavigator = object : MiniAppNavigator {
                override fun openExternalUrl(url: String, externalResultHandler: ExternalResultHandler) {
                    val customTabsIntent = CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .build()
                    customTabsIntent.launchUrl(context, url.toUri())
                }
            }
        }
    }

    override fun runSuccessCallback(callbackId: String, value: String) {
        post {
            evaluateJavascript(
                "MiniAppBridge.execSuccessCallback(`$callbackId`, `${value.replace("`", "\\`")}`)"
            ) {}
        }
    }

    override fun runErrorCallback(callbackId: String, errorMessage: String) {
        post {
            evaluateJavascript(
                "MiniAppBridge.execErrorCallback(`$callbackId`, `${errorMessage.replace("`", "\\`")}`)"
            ) {}
        }
    }

    private fun getWebViewAssetLoader() = WebViewAssetLoader.Builder()
        .setDomain(miniAppScheme.miniAppDomain)
        .addPathHandler(
            "/$SUB_DOMAIN_PATH/", WebViewAssetLoader.InternalStoragePathHandler(
                context,
                File(basePath)
            )
        )
        .addPathHandler(
            "/", WebViewAssetLoader.InternalStoragePathHandler(
                context,
                File(basePath)
            )
        )
        .build()

    internal open fun getLoadUrl(): String {
        val parentUrl = "${miniAppScheme.miniAppCustomDomain}$SUB_DOMAIN_PATH/index.html"
        return miniAppScheme.appendParametersToUrl(parentUrl, queryParams)
    }

    protected open fun getMiniAppWebViewClient(): MiniAppWebViewClient = MiniAppWebViewClient(
        context,
        getWebViewAssetLoader(),
        miniAppNavigator!!,
        externalResultHandler,
        miniAppScheme
    )
}

internal interface WebViewListener {
    fun runSuccessCallback(callbackId: String, value: String)
    fun runErrorCallback(callbackId: String, errorMessage: String)
}
