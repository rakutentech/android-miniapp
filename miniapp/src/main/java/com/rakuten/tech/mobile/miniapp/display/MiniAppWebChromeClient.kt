package com.rakuten.tech.mobile.miniapp.display

import android.content.Context
import android.webkit.GeolocationPermissions
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.JsPromptResult
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.js.DialogType
import java.io.BufferedReader

internal class MiniAppWebChromeClient(
    val context: Context,
    val miniAppInfo: MiniAppInfo
) : WebChromeClient() {

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    @VisibleForTesting
    internal val bridgeJs = try {
        val inputStream = context.assets.open("js-miniapp/js-miniapp-bridge/export/android/bridge.js")
        inputStream.bufferedReader().use(BufferedReader::readText)
    } catch (e: Exception) {
        null
    }

    override fun onReceivedTitle(webView: WebView, title: String?) {
        doInjection(webView)
        super.onReceivedTitle(webView, title)
    }

    @Suppress("FunctionMaxLength")
    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        callback?.invoke(origin, true, false)
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean =
        onShowDialog(
            context = context,
            message = message,
            result = result as JsResult,
            dialogType = DialogType.ALERT,
            miniAppInfo = miniAppInfo
        )

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean =
        onShowDialog(
            context = context,
            message = message,
            result = result as JsResult,
            dialogType = DialogType.CONFIRM,
            miniAppInfo = miniAppInfo
        )

    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean = onShowDialog(
        context = context,
        message = message,
        defaultValue = defaultValue,
        result = result,
        dialogType = DialogType.PROMPT,
        miniAppInfo = miniAppInfo
    )

    @VisibleForTesting
    internal fun doInjection(webView: WebView) {
        webView.evaluateJavascript(bridgeJs) {}
    }
}
