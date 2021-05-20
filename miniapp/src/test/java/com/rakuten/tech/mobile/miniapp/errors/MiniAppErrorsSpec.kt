package com.rakuten.tech.mobile.miniapp.errors

import org.amshove.kluent.shouldBe
import org.junit.Test

class MiniAppErrorsSpec {

    @Test
    fun `should keep the predefined type of custom errors in enum`() {
        AccessTokenErrorType.AudienceNotSupportedError.errorKey shouldBe "AudienceNotSupportedError"
        AccessTokenErrorType.ScopesNotSupportedError.errorKey shouldBe "ScopesNotSupportedError"
        AccessTokenErrorType.AuthorizationFailureError.errorKey shouldBe "AuthorizationFailureError"
    }
}