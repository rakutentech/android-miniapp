package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceError
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import androidx.webkit.WebViewAssetLoader

internal class MiniAppWebViewClient(
    private val context: Context,
    @VisibleForTesting internal val loader: WebViewAssetLoader,
    private val customDomain: String,
    private val customScheme: String
) : WebViewClient() {

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val response = loader.shouldInterceptRequest(request.url)
        interceptMimeType(response, request)
        return response
    }

    @VisibleForTesting
    internal inline fun interceptMimeType(response: WebResourceResponse?, request: WebResourceRequest) {
        response?.let {
            if (request.url != null && request.url.toString().endsWith(".js", true))
                it.mimeType = "application/javascript"
        }
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        if (request.url != null) {
            val requestUrl = request.url.toString()
            if (requestUrl.startsWith(customScheme))
                return onErrorRedirect(view) {
                    loadWithCustomDomain(view, requestUrl.replace(customScheme, customDomain))
                }
            else if (requestUrl.startsWith("tel:"))
                return onErrorRedirect(view) { handleTelLink(requestUrl) }
        }
        super.onReceivedError(view, request, error)
    }

    @Suppress("MagicNumber")
    @VisibleForTesting
    internal inline fun onErrorRedirect(view: WebView, crossinline execution: () -> Unit) {
        view.stopLoading()
        view.postDelayed({ execution.invoke() }, 100)
    }

    @VisibleForTesting
    internal fun loadWithCustomDomain(view: WebView, requestUrl: String) = view.loadUrl(requestUrl)

    @VisibleForTesting
    internal fun handleTelLink(requestUrl: String) = Intent(Intent.ACTION_DIAL).let {
        it.data = requestUrl.toUri()
        context.startActivity(it)
    }
}
