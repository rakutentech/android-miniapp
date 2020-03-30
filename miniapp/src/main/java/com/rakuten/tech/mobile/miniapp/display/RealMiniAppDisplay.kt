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
import com.rakuten.tech.mobile.miniapp.MiniAppMessageInterface
import java.io.File

private const val ASSET_DOMAIN_SUFFIX = "miniapps.androidplatform.net"
private const val SUB_DOMAIN_PATH = "miniapp"
private const val MINI_APP_INTERFACE = "MiniApp"

@SuppressLint("SetJavaScriptEnabled")
internal class RealMiniAppDisplay(
    context: Context,
    val basePath: String,
    val appId: String,
    val miniAppMessageInterface: MiniAppMessageInterface
) : MiniAppDisplay, WebView(context) {

    private val miniAppDomain = "$appId.$ASSET_DOMAIN_SUFFIX"

    init {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )

        addJavascriptInterface(miniAppMessageInterface, MINI_APP_INTERFACE)
        settings.javaScriptEnabled = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        webViewClient = MiniAppWebViewClient(getWebViewAssetLoader())

        loadUrl(getLoadUrl())
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

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? = loader.shouldInterceptRequest(request.url)

    override fun onPageFinished(webView: WebView, url: String) {
        webView.loadUrl("javascript:function showUniqueId(){" +
                "document.getElementById(\"version\").innerHTML = \"Injected.\";}")
    }
}
