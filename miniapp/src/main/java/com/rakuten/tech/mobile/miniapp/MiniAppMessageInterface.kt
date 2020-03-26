package com.rakuten.tech.mobile.miniapp

import android.webkit.JavascriptInterface

/** Interface for communicating with mini app. **/
interface MiniAppMessageInterface {
    /** Get provided id of mini app for any purpose. **/
    @JavascriptInterface
    fun getUniqueId(): String
}
