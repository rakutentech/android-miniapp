package com.rakuten.tech.mobile.miniapp.errors

import com.rakuten.tech.mobile.miniapp.js.hostenvironment.HostEnvironmentInfoError
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test
import kotlin.test.assertTrue

class MiniAppErrorSpec {

    @Test
    fun `authorizationFailureError should have the custom message properly`() {
        MiniAppAccessTokenError.authorizationFailureError("test_message") shouldBeInstanceOf
                MiniAppAccessTokenError::class
    }

    @Test
    fun `MiniAppPointsError should have the custom message properly`() {
        val pointError = MiniAppPointsError.custom("test_message")
        assertTrue { pointError.message == "test_message" }
    }

    @Test
    fun `HostEnvironmentInfoError should have the custom message properly`() {
        val hostEnvError = HostEnvironmentInfoError.custom("test_message")
        assertTrue { hostEnvError.message == "test_message" }
    }
}
