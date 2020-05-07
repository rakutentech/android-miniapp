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
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.webkit.WebViewAssetLoader
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import java.io.BufferedReader
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

private const val SUB_DOMAIN_PATH = "miniapp"
private const val MINI_APP_INTERFACE = "MiniAppAndroid"

@SuppressLint("SetJavaScriptEnabled")
internal class RealMiniAppDisplay(
    context: Context,
    val basePath: String,
    val appId: String,
    miniAppMessageBridge: MiniAppMessageBridge
) : MiniAppDisplay, WebView(context), WebViewListener {

    private val miniAppDomain = "mscheme.$appId"
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
        webViewClient = MiniAppWebViewClient(context, getWebViewAssetLoader(), customDomain, customScheme)

        loadUrl(getLoadUrl())
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
    internal fun getLoadUrl() = "$customScheme$SUB_DOMAIN_PATH/index.html"
}

@VisibleForTesting
internal class MiniAppWebViewClient(
    context: Context,
    @VisibleForTesting internal val loader: WebViewAssetLoader,
    private val customDomain: String,
    private val customScheme: String
) : WebViewClient() {
    @VisibleForTesting
    internal var isJsInjected: AtomicBoolean = AtomicBoolean(false)

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    @VisibleForTesting
    internal val bridgeJs = try {
        val inputStream = context.assets.open("bridge.js")
        inputStream.bufferedReader().use(BufferedReader::readText)
    } catch (e: Exception) {
        null
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        if (request.url != null && request.url.toString().startsWith(customScheme)) {
            // Do js injection when index is loaded and at least one resource is requested to trigger this.
            doInjection(view, request.url.toString())

            val interceptUri = request.url.toString().replace(customScheme, customDomain).toUri()
            return loader.shouldInterceptRequest(interceptUri)
        }
        return loader.shouldInterceptRequest(request.url)
    }

    @VisibleForTesting
    internal fun doInjection(webView: WebView, urlRequest: String) {
        if (!isJsInjected.get() && !urlRequest.endsWith("index.html")) {
            webView.post { webView.evaluateJavascript(bridgeJs) {} }
            isJsInjected.set(true)
        }
    }
}

internal interface WebViewListener {
    fun runSuccessCallback(callbackId: String, value: String)
    fun runErrorCallback(callbackId: String, errorMessage: String)
}
