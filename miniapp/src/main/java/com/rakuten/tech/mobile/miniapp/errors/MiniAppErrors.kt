package com.rakuten.tech.mobile.miniapp.errors

enum class AccessTokenErrorType(val error_key: String) {
    AudienceNotSupportedError("AudienceNotSupportedError"),
    ScopesNotSupportedError("ScopesNotSupportedError"),
    AuthorizationFailureError("AuthorizationFailureError");
}
