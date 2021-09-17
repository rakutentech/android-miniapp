package com.rakuten.tech.mobile.miniapp.js.hostAppInfo

import com.rakuten.tech.mobile.miniapp.errors.MiniAppBridgeError

/**
 * A class to provide the custom errors specific for host environment info.
 */
class HostEnvironmentInfoError(val type: String? = null, val message: String? = null) :
        MiniAppBridgeError(type, message) {

    companion object {
        /**
         *  send custom error message from host app.
         *  @property message error message send to mini app.
         */
        fun custom(message: String) = HostEnvironmentInfoError(message = message)
    }
}
