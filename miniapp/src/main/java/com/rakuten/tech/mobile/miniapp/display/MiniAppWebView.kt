package com.rakuten.tech.mobile.miniapp.display

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.webkit.WebViewAssetLoader
import com.rakuten.tech.mobile.miniapp.js.MiniAppCode
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import java.io.File

private const val SUB_DOMAIN_PATH = "miniapp"
private const val MINI_APP_INTERFACE = "MiniAppAndroid"

@SuppressLint("SetJavaScriptEnabled")
internal class MiniAppWebView(
    context: Context,
    val basePath: String,
    val appId: String,
    miniAppMessageBridge: MiniAppMessageBridge
) : WebView(context), WebViewListener, WebChromeListener {

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
        settings.setGeolocationEnabled(true)
        webViewClient = MiniAppWebViewClient(getWebViewAssetLoader(), customDomain, customScheme)
        webChromeClient = MiniAppWebChromeClient(context, this)

        loadUrl(getLoadUrl())
    }

    fun destroyView() {
        stopLoading()
        webViewClient = null
        destroyGeolocationPrompt()
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

    // region geolocation
    private var geoLocationRequestOrigin: String? = null
    private var geoLocationCallback: GeolocationPermissions.Callback? = null

    override fun onGeolocationPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
        geoLocationRequestOrigin = origin
        geoLocationCallback = callback
    }

    override fun onGeolocationPermissionResult(isGranted: Boolean) {
        geoLocationCallback?.invoke(geoLocationRequestOrigin, isGranted, isGranted)
        destroyGeolocationPrompt()
    }

    @VisibleForTesting
    internal fun destroyGeolocationPrompt() {
        geoLocationRequestOrigin = null
        geoLocationCallback = null
    }
    // end region

    override fun onRequestPermissionsResult(requestCode: Int, permission: String, grantResult: Int) {
        if (requestCode == MiniAppCode.Permission.GEOLOCATION &&
            permission == Manifest.permission.ACCESS_FINE_LOCATION)
            onGeolocationPermissionResult(grantResult == PackageManager.PERMISSION_GRANTED)
    }

    private fun getWebViewAssetLoader() = WebViewAssetLoader.Builder()
        .setDomain(miniAppDomain)
        .addPathHandler(
            "/$SUB_DOMAIN_PATH/", WebViewAssetLoader.InternalStoragePathHandler(
                context,
                File(basePath)
            )
        )
        .addPathHandler(
            "/", WebViewAssetLoader.InternalStoragePathHandler(
                context,
                File(basePath)
            )
        )
        .build()

    @VisibleForTesting
    internal fun getLoadUrl() = "$customDomain$SUB_DOMAIN_PATH/index.html"
}

internal interface WebViewListener {
    fun runSuccessCallback(callbackId: String, value: String)
    fun runErrorCallback(callbackId: String, errorMessage: String)
    fun onRequestPermissionsResult(requestCode: Int, permission: String, grantResult: Int)
}
