qpackage com.rakuten.tech.mobile.miniapp.errors

/** Type of errors for access token fetch **/
@Suppress("MatchingDeclarationName")
enum class AccessTokenErrorType(val errorKey: String) {
    AudienceNotSupportedError("AudienceNotSupportedError"),
    ScopesNotSupportedError("ScopesNotSupportedError"),
    AuthorizationFailureError("AuthorizationFailureError");
}
