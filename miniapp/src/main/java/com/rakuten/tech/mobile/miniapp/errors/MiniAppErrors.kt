package com.rakuten.tech.mobile.miniapp.errors

/** Enumeration that is used to return  Access Token error. **/
enum class AccessTokenErrorType {
    // Requested Audience is not supported
    AudienceNotSupportedError,
    // Requested Scope is not supported
    ScopesNotSupportedError,
    // Authorization failed and the reason will be shared by the host app
    AuthorizationFailureError,
    // Unknown/Custom error
    Error;

    internal companion object {
        internal fun getValue(type: AccessTokenErrorType): String {
            return when (type) {
                AudienceNotSupportedError -> "AudienceNotSupportedError"
                ScopesNotSupportedError -> "ScopesNotSupportedError"
                AuthorizationFailureError -> "AuthorizationFailureError"
                Error -> "Error"
            }
        }
    }
}
