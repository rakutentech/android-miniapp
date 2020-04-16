package com.rakuten.tech.mobile.miniapp.js

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.display.WebViewListener

/** Bridge interface for communicating with mini app. **/
abstract class MiniAppMessageBridge {
    private lateinit var webViewListener: WebViewListener

    /** Get provided id of mini app for any purpose. **/
    abstract fun getUniqueId(callbackId: String)

    /** Handle the message from external. **/
    @JavascriptInterface
    fun postMessage(jsonStr: String) {
        val callbackObj = Gson().fromJson(jsonStr, CallbackObj::class.java)
        when (callbackObj.action) {
            "getUniqueId" -> getUniqueId(callbackObj.id)
        }
    }

    /** Return a value to mini app. **/
    fun postValue(callbackId: String, value: String) {
        webViewListener.runSuccessCallback(callbackId, value)
    }

    /** Emit an error to mini app. **/
    fun postError(callbackId: String, errorMessage: String) {
        webViewListener.runErrorCallback(callbackId, errorMessage)
    }

    internal fun setWebViewListener(webViewListener: WebViewListener) {
        this.webViewListener = webViewListener
    }
}
