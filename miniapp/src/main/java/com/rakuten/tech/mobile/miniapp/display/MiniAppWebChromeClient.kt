package com.rakuten.tech.mobile.miniapp.display

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.JsResult
import android.webkit.JsPromptResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.js.DialogType
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import java.io.BufferedReader

@Suppress("TooManyFunctions", "LargeClass")
internal class MiniAppWebChromeClient(
    private val context: Context,
    private val miniAppInfo: MiniAppInfo,
    val miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
    private val miniAppFileChooser: MiniAppFileChooser?
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
        if (miniAppCustomPermissionCache.hasPermission(
                miniAppInfo.id,
                MiniAppCustomPermissionType.LOCATION
            )
        ) callback?.invoke(origin, true, false)
        else callback?.invoke(origin, false, false)
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
            miniAppTitle = miniAppInfo.displayName
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
            miniAppTitle = miniAppInfo.displayName
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
        miniAppTitle = miniAppInfo.displayName
    )

    @VisibleForTesting
    internal fun doInjection(webView: WebView) {
        if (bridgeJs !== null) {
            webView.evaluateJavascript(bridgeJs) {}
        }
    }

    //region fullscreen video
    @VisibleForTesting
    internal var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private var originalSystemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    private val fullScreenFlag = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE

    override fun onShowCustomView(paramView: View?, paramCustomViewCallback: CustomViewCallback?) {
        if (customView != null) {
            onHideCustomView()
            return
        }
        customView = paramView
        if (context is Activity) {
            context.apply {
                originalSystemUiVisibility = window.decorView.systemUiVisibility
                customViewCallback = paramCustomViewCallback
                (window.decorView as FrameLayout).addView(customView, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                window.decorView.systemUiVisibility = fullScreenFlag
                customView?.setBackgroundColor(getColor(android.R.color.black))
                customView?.setOnSystemUiVisibilityChangeListener { updateControls() }
            }
        }
    }

    override fun onHideCustomView() {
        if (context is Activity) {
            context.apply {
                (window.decorView as FrameLayout).removeView(customView)
                customView = null
                window.decorView.systemUiVisibility = originalSystemUiVisibility
                customViewCallback!!.onCustomViewHidden()
                customViewCallback = null
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

    fun onWebViewDetach() {
        if (customView != null)
            onHideCustomView()
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        return miniAppFileChooser?.onShowFileChooser(filePathCallback, fileChooserParams, context)
            ?: false
    }
}
