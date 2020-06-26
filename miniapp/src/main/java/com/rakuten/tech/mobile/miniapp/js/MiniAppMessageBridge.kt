package com.rakuten.tech.mobile.miniapp.js

import android.app.Activity
import android.content.Context
import android.webkit.JavascriptInterface
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.display.WebViewListener

/** Bridge interface for communicating with mini app. **/
abstract class MiniAppMessageBridge(val context: Context) {
    private lateinit var webViewListener: WebViewListener

    /** Get provided id of mini app for any purpose. **/
    abstract fun getUniqueId(): String

    /** Handle the message from external. **/
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    @JavascriptInterface
    fun postMessage(jsonStr: String) {
        val callbackObj = Gson().fromJson(jsonStr, CallbackObj::class.java)
        when (callbackObj.action) {
            "getUniqueId" -> try {
                postValue(callbackObj.id, getUniqueId())
            } catch (e: Exception) {
                postError(callbackObj.id, "Cannot get unique id: ${e.message}")
            }
        }
    }

    @JavascriptInterface
    fun requestPermissions(permissions: Array<String>, requestCode: Int) =
        ActivityCompat.requestPermissions(context as Activity, permissions, requestCode)

    /** Inform the permission request result to MiniApp. **/
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (permissions.size == grantResults.size) {
            for (i in permissions.indices)
                webViewListener.onRequestPermissionsResult(requestCode, permissions[i], grantResults[i])
        }
    }

    /** Return a value to mini app. **/
    internal fun postValue(callbackId: String, value: String) {
        webViewListener.runSuccessCallback(callbackId, value)
    }

    /** Emit an error to mini app. **/
    internal fun postError(callbackId: String, errorMessage: String) {
        webViewListener.runErrorCallback(callbackId, errorMessage)
    }

    internal fun setWebViewListener(webViewListener: WebViewListener) {
        this.webViewListener = webViewListener
    }
}
