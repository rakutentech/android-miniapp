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
import com.rakuten.tech.mobile.miniapp.MiniAppScheme
import com.rakuten.tech.mobile.miniapp.navigator.ExternalResultHandler
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator

internal class MiniAppWebViewClient(
    private val context: Context,
    @VisibleForTesting internal val loader: WebViewAssetLoader,
    private val miniAppNavigator: MiniAppNavigator?,
    private val externalResultHandler: ExternalResultHandler,
    private val miniAppScheme: MiniAppScheme
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
            } else if (!miniAppScheme.isMiniAppUrl(requestUrl)) {
                // check if there is navigator implementation on miniapp.
                if (miniAppNavigator != null) {
                    miniAppNavigator.openExternalUrl(requestUrl, externalResultHandler)
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
        if (request.url != null && request.url.toString().startsWith(miniAppScheme.miniAppCustomScheme)) {
            loadWithCustomDomain(
                view,
                request.url.toString().replace(miniAppScheme.miniAppCustomScheme, miniAppScheme.miniAppCustomDomain)
            )
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
