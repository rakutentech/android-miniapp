package com.rakuten.tech.mobile.miniapp.display

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.webkit.WebViewAssetLoader
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.MiniAppScheme
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.ExternalResultHandler
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import java.io.File

private const val SUB_DOMAIN_PATH = "miniapp"
private const val MINI_APP_INTERFACE = "MiniAppAndroid"

@SuppressLint("SetJavaScriptEnabled")
internal class MiniAppWebView(
    context: Context,
    val basePath: String,
    val miniAppInfo: MiniAppInfo,
    val miniAppMessageBridge: MiniAppMessageBridge,
    miniAppNavigator: MiniAppNavigator?,
    val hostAppUserAgentInfo: String,
    val miniAppWebChromeClient: MiniAppWebChromeClient = MiniAppWebChromeClient(context, miniAppInfo),
    val miniAppCustomPermissionCache: MiniAppCustomPermissionCache
) : WebView(context), WebViewListener {

    private val miniAppScheme = MiniAppScheme(miniAppInfo.id)

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
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )

        settings.javaScriptEnabled = true
        addJavascriptInterface(miniAppMessageBridge, MINI_APP_INTERFACE)

        miniAppMessageBridge.init(
            activity = context as Activity,
            webViewListener = this,
            customPermissionCache = miniAppCustomPermissionCache,
            miniAppInfo = miniAppInfo
        )

        settings.allowUniversalAccessFromFileURLs = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true

        if (hostAppUserAgentInfo.isNotEmpty())
            settings.userAgentString =
                String.format("%s %s", settings.userAgentString, hostAppUserAgentInfo)

        webViewClient = MiniAppWebViewClient(context, getWebViewAssetLoader(), miniAppNavigator,
            externalResultHandler, miniAppScheme)
        webChromeClient = miniAppWebChromeClient

        loadUrl(getLoadUrl())
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun destroyView() = try {
        stopLoading()
        webViewClient = null
        miniAppWebChromeClient.destroy()
        webChromeClient = null
        miniAppMessageBridge.screenBridgeDispatcher.releaseLock()
        destroy()
    } catch (e: Exception) {
        // The activity may release before those executions.
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
                "MiniAppBridge.execErrorCallback(\"$callbackId\", \"$errorMessage\")"
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

    @VisibleForTesting
    internal fun getLoadUrl() = "${miniAppScheme.miniAppCustomDomain}$SUB_DOMAIN_PATH/index.html"
}

internal interface WebViewListener {
    fun runSuccessCallback(callbackId: String, value: String)
    fun runErrorCallback(callbackId: String, errorMessage: String)
}
