package com.rakuten.tech.mobile.miniapp.display

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.JsResult
import android.webkit.JsPromptResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
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
        val inputStream = context.assets.open("js-miniapp/bridge.js")
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

    //region fullscreen video
    @VisibleForTesting
    internal var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private var originalOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    private var originalSystemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    private val fullScreenFlag = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    // When webview enters fullscreen.
    override fun onShowCustomView(paramView: View?, paramCustomViewCallback: CustomViewCallback?) {
        if (customView != null) {
            onHideCustomView()
            return
        }
        customView = paramView
        if (context is Activity) {
            context.apply {
                originalSystemUiVisibility = window.decorView.systemUiVisibility
                originalOrientation = requestedOrientation
                customViewCallback = paramCustomViewCallback
                (window.decorView as FrameLayout).addView(customView, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                window.decorView.systemUiVisibility = fullScreenFlag
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
                customView?.setOnSystemUiVisibilityChangeListener { updateControls() }
            }
        }
    }

    // When webview exits fullscreen.
    override fun onHideCustomView() {
        if (context is Activity) {
            context.apply {
                (window.decorView as FrameLayout).removeView(customView)
                customView = null
                window.decorView.systemUiVisibility = originalSystemUiVisibility
                requestedOrientation = originalOrientation
                customViewCallback!!.onCustomViewHidden()
                customViewCallback = null
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
            }
        }
    }

    @VisibleForTesting
    internal fun updateControls() {
        customView?.let {
            val params = (it.layoutParams as FrameLayout.LayoutParams).apply {
                bottomMargin = 0
                topMargin = 0
                leftMargin = 0
                rightMargin = 0
                height = ViewGroup.LayoutParams.MATCH_PARENT
                width = ViewGroup.LayoutParams.MATCH_PARENT
            }
            it.layoutParams = params
            if (context is Activity)
                context.window.decorView.systemUiVisibility = fullScreenFlag
        }
    }
    // end region video fullscreen
}
