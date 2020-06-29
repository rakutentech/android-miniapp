package com.rakuten.tech.mobile.miniapp.display

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import com.rakuten.tech.mobile.miniapp.js.MiniAppPermission
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import java.io.BufferedReader

internal class MiniAppWebChromeClient(
    val context: Context,
    val miniAppMessageBridge: MiniAppMessageBridge
) : WebChromeClient(), WebChromeListener {

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    @VisibleForTesting
    internal val bridgeJs = try {
        val inputStream = context.assets.open("bridge.js")
        inputStream.bufferedReader().use(BufferedReader::readText)
    } catch (e: Exception) {
        null
    }

    override fun onReceivedTitle(webView: WebView, title: String?) {
        doInjection(webView)
        super.onReceivedTitle(webView, title)
    }

    // region geolocation
    private var geoLocationRequestOrigin: String? = null
    @VisibleForTesting
    internal var geoLocationCallback: GeolocationPermissions.Callback? = null

    @Suppress("FunctionMaxLength")
    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        geoLocationRequestOrigin = origin
        geoLocationCallback = callback
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            onGeolocationPermissionResult(true)
        else
            miniAppMessageBridge.requestPermission(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MiniAppPermission.GEOLOCATION
            )
    }

    override fun onGeolocationPermissionResult(isGranted: Boolean) {
        if (geoLocationCallback != null)
            geoLocationCallback?.invoke(geoLocationRequestOrigin, isGranted, isGranted)
        geoLocationRequestOrigin = null
        geoLocationCallback = null
    }
    // end region

    @VisibleForTesting
    internal fun doInjection(webView: WebView) {
        webView.evaluateJavascript(bridgeJs) {}
    }
}

internal interface WebChromeListener {
    fun onGeolocationPermissionResult(isGranted: Boolean)
}
