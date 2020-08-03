package com.rakuten.tech.mobile.miniapp.display

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.webkit.WebViewAssetLoader
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import java.io.File

private const val SUB_DOMAIN_PATH = "miniapp"
private const val MINI_APP_INTERFACE = "MiniAppAndroid"

@SuppressLint("SetJavaScriptEnabled")
internal class MiniAppWebView(
    context: Context,
    val basePath: String,
    val miniAppInfo: MiniAppInfo,
    miniAppMessageBridge: MiniAppMessageBridge,
    val hostAppUserAgentInfo: String,
    val miniAppWebChromeClient: MiniAppWebChromeClient = MiniAppWebChromeClient(context, miniAppInfo)
) : WebView(context), WebViewListener {

    private val miniAppDomain = "mscheme.${miniAppInfo.id}"
    private val customScheme = "$miniAppDomain://"
    private val customDomain = "https://$miniAppDomain/"

    init {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )

        settings.javaScriptEnabled = true
        addJavascriptInterface(miniAppMessageBridge, MINI_APP_INTERFACE)
        miniAppMessageBridge.setWebViewListener(this)

        settings.allowUniversalAccessFromFileURLs = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true

        if (hostAppUserAgentInfo.isNotEmpty())
            settings.userAgentString =
                String.format("%s %s", settings.userAgentString, hostAppUserAgentInfo)

        webViewClient = MiniAppWebViewClient(context, getWebViewAssetLoader(), customDomain, customScheme)
        webChromeClient = miniAppWebChromeClient

        loadUrl(getLoadUrl())
    }

    fun destroyView() {
        stopLoading()
        webViewClient = null
        destroy()
    }

    override fun runSuccessCallback(callbackId: String, value: String) {
        post {
            evaluateJavascript(
                "MiniAppBridge.execSuccessCallback(\"$callbackId\", \"$value\")"
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
        .setDomain(miniAppDomain)
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
    internal fun getLoadUrl() = "$customDomain$SUB_DOMAIN_PATH/index.html"
}

internal interface WebViewListener {
    fun runSuccessCallback(callbackId: String, value: String)
    fun runErrorCallback(callbackId: String, errorMessage: String)
}
