package com.rakuten.tech.mobile.miniapp.display

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.webkit.WebViewAssetLoader
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import java.io.BufferedReader
import java.io.File

private const val ASSET_DOMAIN_SUFFIX = "miniapps.androidplatform.net"
private const val SUB_DOMAIN_PATH = "miniapp"
private const val MINI_APP_INTERFACE = "MiniAppAndroid"

@SuppressLint("SetJavaScriptEnabled")
internal class RealMiniAppDisplay(
    context: Context,
    val basePath: String,
    val appId: String,
    miniAppMessageBridge: MiniAppMessageBridge
) : MiniAppDisplay, WebView(context), WebViewListener {

    private val miniAppDomain = "$appId.$ASSET_DOMAIN_SUFFIX"

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
        webViewClient = MiniAppWebViewClient(context, getWebViewAssetLoader())

        loadUrl(getLoadUrl())
    }

    override fun setWebViewClient(client: WebViewClient?) {
        super.setWebViewClient(client ?: MiniAppWebViewClient(context, getWebViewAssetLoader()))
    }

    override fun getMiniAppView(): View = this

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun destroyView() {
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
        .build()

    @VisibleForTesting
    internal fun getLoadUrl() = "https://$miniAppDomain/$SUB_DOMAIN_PATH/index.html"
}

@VisibleForTesting
internal class MiniAppWebViewClient(
    context: Context,
    private val loader: WebViewAssetLoader
) : WebViewClient() {
    @VisibleForTesting
    internal var isJsInjected = false
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private val bridgeJs = try {
        val inputStream = context.assets.open("bridge.js")
        inputStream.bufferedReader().use(BufferedReader::readText)
    } catch (e: Exception) {
        null
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? = loader.shouldInterceptRequest(request.url)

    override fun onLoadResource(webView: WebView, url: String?) {
        super.onLoadResource(webView, url)
        if (!isJsInjected) {
            webView.evaluateJavascript(bridgeJs) {}
            isJsInjected = true
        }
    }
}

internal interface WebViewListener {
    fun runSuccessCallback(callbackId: String, value: String)
    fun runErrorCallback(callbackId: String, errorMessage: String)
}
