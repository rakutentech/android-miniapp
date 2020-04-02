package com.rakuten.tech.mobile.miniapp.js

import android.webkit.JavascriptInterface
import com.google.gson.Gson

/** Interface for communicating with mini app. **/
interface MiniAppMessageInterface {
    /** Get provided id of mini app for any purpose. **/
    @JavascriptInterface
    fun getUniqueId(jsonStr: String)

    companion object {
        fun getCallbackObjFromJson(jsonStr: String): CallbackObj = Gson().fromJson(jsonStr, CallbackObj::class.java)
    }
}
