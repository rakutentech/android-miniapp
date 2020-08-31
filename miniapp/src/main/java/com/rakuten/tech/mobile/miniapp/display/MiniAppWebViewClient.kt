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
import com.rakuten.tech.mobile.miniapp.navigator.ExternalUrlHandler
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator

internal class MiniAppWebViewClient(
    private val context: Context,
    @VisibleForTesting internal val loader: WebViewAssetLoader,
    private val miniAppNavigator: MiniAppNavigator?,
    private val externalUrlHandler: ExternalUrlHandler,
    private val customDomain: String,
    private val customScheme: String
) : WebViewClient() {

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val response = loader.shouldInterceptRequest(request.url)
        interceptMimeType(response, request)
        return response
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        var shouldCancelLoading = super.shouldOverrideUrlLoading(view, request)
        if (request.url != null) {
            val requestUrl = request.url.toString()
            if (requestUrl.startsWith("tel:")) {
                openPhoneDialer(requestUrl)
                shouldCancelLoading = true
            } else if (!(requestUrl.startsWith(customDomain) || requestUrl.startsWith(customScheme))) {
                // check if there is navigator implementation on miniapp.
                if (miniAppNavigator != null) {
                    miniAppNavigator.openExternalUrl(requestUrl, externalUrlHandler)
                    shouldCancelLoading = true
                }
            }
        }
        return shouldCancelLoading
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        if (request.url != null && request.url.toString().startsWith(customScheme)) {
            loadWithCustomDomain(view, request.url.toString().replace(customScheme, customDomain))
            return
        }
        super.onReceivedError(view, request, error)
    }

    @VisibleForTesting
    internal fun interceptMimeType(response: WebResourceResponse?, request: WebResourceRequest) {
        response?.let {
            if (request.url != null && request.url.toString().endsWith(".js", true))
                it.mimeType = "application/javascript"
        }
    }

    @Suppress("MagicNumber")
    @VisibleForTesting
    internal fun loadWithCustomDomain(view: WebView, requestUrl: String) {
        view.stopLoading()
        view.postDelayed(
            { view.loadUrl(requestUrl) },
            100
        )
    }

    @VisibleForTesting
    internal fun openPhoneDialer(requestUrl: String) = Intent(Intent.ACTION_DIAL).let {
        it.data = requestUrl.toUri()
        context.startActivity(it)
    }
}
