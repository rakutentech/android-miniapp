package com.rakuten.tech.mobile.miniapp.errors

/**
 * A class to provide the custom errors specific for user's points.
 */
class MiniAppPointsError(val type: String? = null, val message: String? = null) :
    MiniAppBridgeError(type, message) {

    companion object {
        /**
         *  send custom error message from host app.
         *  @property message error message send to mini app.
         */
        fun custom(message: String) = MiniAppPointsError(message = message)
    }
}
