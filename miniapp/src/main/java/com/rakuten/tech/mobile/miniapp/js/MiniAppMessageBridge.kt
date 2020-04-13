package com.rakuten.tech.mobile.miniapp.js

import android.webkit.JavascriptInterface
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay

/** Bridge interface for communicating with mini app. **/
abstract class MiniAppMessageBridge(val view: MiniAppDisplay) {
    /** Get provided id of mini app for any purpose. **/
    abstract fun getUniqueId(): String

    /** Handle the message from external. **/
    @JavascriptInterface
    fun postMessage(jsonStr: String) {
        val callbackObj = Gson().fromJson(jsonStr, CallbackObj::class.java)
        when (callbackObj.action) {
            "getUniqueId" -> postValue(callbackObj.id, getUniqueId())
        }
    }

    /** Return a value to mini app. **/
    @VisibleForTesting
    internal fun postValue(callbackId: String, value: String) {
        view.runJsAsyncCallback(callbackId, value)
    }
}
