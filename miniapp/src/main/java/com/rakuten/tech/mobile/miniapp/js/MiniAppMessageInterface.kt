package com.rakuten.tech.mobile.miniapp.js

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniAppDisplay

/** Interface for communicating with mini app. **/
interface MiniAppMessageInterface {
    /** Get provided id of mini app for any purpose. **/
    @JavascriptInterface
    fun getUniqueId(jsonStr: String)

    /** Return a provided id to mini app. **/
    fun postUniqueId(jsonStr: String, uniqueId: String, view: MiniAppDisplay) {
        val callbackObj = Gson().fromJson(jsonStr, CallbackObj::class.java)
        view.runJsAsyncCallback(callbackObj.id, "example_unique_id")
    }
}
