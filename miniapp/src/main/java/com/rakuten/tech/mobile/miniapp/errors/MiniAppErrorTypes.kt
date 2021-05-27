package com.rakuten.tech.mobile.miniapp.errors

/** Enumeration that is used to return  Access Token error. **/
enum class AccessTokenErrorType(val type: String) {
    // Requested Audience is not supported
    AudienceNotSupportedError("AudienceNotSupportedError"),
    // Requested Scope is not supported
    ScopesNotSupportedError("ScopesNotSupportedError"),
    // Authorization failed and the reason will be shared by the host app
    AuthorizationFailureError("AuthorizationFailureError"),
    // Unknown/Custom error
    Error("Error");
}
