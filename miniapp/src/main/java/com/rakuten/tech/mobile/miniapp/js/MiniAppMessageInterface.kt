package com.rakuten.tech.mobile.miniapp.js

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay

/** Interface for communicating with mini app. **/
interface MiniAppMessageInterface {
    /** Get provided id of mini app for any purpose. **/
    @JavascriptInterface
    fun getUniqueId(jsonStr: String)

    /** Return a value to mini app. **/
    fun postValue(jsonStr: String, view: MiniAppDisplay, value: String) {
        val callbackObj = Gson().fromJson(jsonStr, CallbackObj::class.java)
        view.runJsAsyncCallback(callbackObj.id, value)
    }
}
