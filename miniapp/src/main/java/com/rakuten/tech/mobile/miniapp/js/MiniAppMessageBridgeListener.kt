package com.rakuten.tech.mobile.miniapp.js

/**
 * Post value and error responses to the mini app.
 */
interface MiniAppMessageBridgeListener {

    /** Emit a value to mini app. **/
    fun postValue(callbackId: String, value: String)

    /** Emit an error response to mini app. **/
    fun postError(callbackId: String, errorMessage: String)
}
