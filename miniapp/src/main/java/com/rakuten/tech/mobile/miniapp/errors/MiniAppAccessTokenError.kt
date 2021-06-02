package com.rakuten.tech.mobile.miniapp.errors

/**
 * A class to provide the custom errors specific for access token.
 */
class MiniAppAccessTokenError(val type: String? = null, val message: String? = null) :
    MiniAppBridgeError(type, message) {

    companion object {
        private const val AudienceNotSupportedError = "AudienceNotSupportedError"
        private const val ScopesNotSupportedError = "ScopesNotSupportedError"
        private const val AuthorizationFailureError = "AuthorizationFailureError"

        // Requested Audience is not supported.
        val audienceNotSupportedError = MiniAppAccessTokenError(type = AudienceNotSupportedError)

        // Requested Scope is not supported.
        val scopesNotSupportedError = MiniAppAccessTokenError(type = ScopesNotSupportedError)

        // Authorization failed and the reason will be shared by the host app.
        val authorizationFailureError = MiniAppAccessTokenError(type = AuthorizationFailureError)

        /**
         *  send custom error message from host app.
         *  @property message error message send to min app.
         */
        fun custom(message: String) = MiniAppAccessTokenError(message = message)
    }
}
