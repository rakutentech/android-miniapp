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
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageInterface
import java.io.BufferedReader
import java.io.File

private const val ASSET_DOMAIN_SUFFIX = "miniapps.androidplatform.net"
private const val SUB_DOMAIN_PATH = "miniapp"
private const val MINI_APP_INTERFACE = "MiniAppAndroid"

@SuppressLint("SetJavaScriptEnabled")
internal class RealMiniAppDisplay(
    context: Context,
    val basePath: String,
    val appId: String
) : MiniAppDisplay, WebView(context) {

    private val miniAppDomain = "$appId.$ASSET_DOMAIN_SUFFIX"

    init {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )

        settings.javaScriptEnabled = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        webViewClient = MiniAppWebViewClient(getWebViewAssetLoader())

        loadUrl(getLoadUrl())
    }

    override fun injectJSInterface(miniAppMessageInterface: MiniAppMessageInterface) {
        addJavascriptInterface(miniAppMessageInterface, MINI_APP_INTERFACE)
    }

    override fun setWebViewClient(client: WebViewClient?) {
        super.setWebViewClient(client ?: MiniAppWebViewClient(getWebViewAssetLoader()))
    }

    override fun getMiniAppView(): View = this

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun destroyView() {
        stopLoading()
        webViewClient = null
        destroy()
    }

    override fun runJsAsyncCallback(callbackId: String, value: String) {
        post {
            evaluateJavascript("javascript:(function (){MiniAppBridge.execCallback(\"$callbackId\", \"$value\")})()") {}
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
internal class MiniAppWebViewClient(private val loader: WebViewAssetLoader) : WebViewClient() {

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val interceptedWebRequest = loader.shouldInterceptRequest(request.url)
        interceptedWebRequest?.let {
            if (request.url.toString().endsWith("js", true)) {
                it.mimeType = "text/javascript"
            }
        }
        return interceptedWebRequest
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun onPageFinished(webView: WebView, url: String?) {
        super.onPageFinished(webView, url)
        try {
            val inputStream = webView.context.assets.open("inject.js")
            inputStream.bufferedReader().use(BufferedReader::readText)
        } catch (e: Exception) {
            null
        }?.let {
            webView.evaluateJavascript("javascript:(function (){$it})()") {}
        }
    }
}
