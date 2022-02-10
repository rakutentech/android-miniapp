package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.js.ErrorBridgeMessage
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class MiniAppSdkExceptionSpec {

    @Test
    fun `MiniAppHasNoPublishedVersionException should provide proper error message`() {
        val exception = MiniAppHasNoPublishedVersionException(TEST_MA_ID)
        exception.message shouldBeEqualTo "Server returned no published version info for the provided Mini App Id: $TEST_MA_ID"
    }

    @Test
    fun `SSLCertificatePinningException should provide proper error message`() {
        val exception = SSLCertificatePinningException("message")
        exception.message shouldBeEqualTo "message: SSL public key mismatched."
    }

    @Test
    fun `MiniAppNotFoundException should provide proper error message`() {
        val exception = MiniAppNotFoundException("message")
        exception.message shouldBeEqualTo "message: Server returned no mini app for the provided project ID."
    }

    @Test
    fun `MiniAppHostException should provide proper error message`() {
        val exception = MiniAppHostException("message")
        exception.message shouldBeEqualTo "message: Only the correct host has access to this token."
    }

    @Test
    fun `MiniAppVerificationException should provide proper error message`() {
        val exception = MiniAppVerificationException("message")
        exception.message shouldBeEqualTo "MiniApp SDK cannot proceed due to security validation: message"
    }

    @Test
    fun `DevicePermissionsNotImplementedException should provide proper error message`() {
        val exception = DevicePermissionsNotImplementedException()
        exception.message shouldBeEqualTo ErrorBridgeMessage.NO_IMPLEMENT_DEVICE_PERMISSION
    }

    @Test
    fun `CustomPermissionsNotImplementedException should provide proper error message`() {
        val exception = CustomPermissionsNotImplementedException()
        exception.message shouldBeEqualTo ErrorBridgeMessage.NO_IMPLEMENT_CUSTOM_PERMISSION
    }

    @Test
    fun `RequiredPermissionsNotGrantedException should provide proper error message`() {
        val exception = RequiredPermissionsNotGrantedException(TEST_MA_ID, TEST_MA_VERSION_ID)
        exception.message shouldBeEqualTo "Mini App has not been granted all of the required permissions " +
                "for the provided Mini App Id: $TEST_MA_ID and the version id: $TEST_MA_VERSION_ID"
    }

    @Test
    fun `sdkExceptionForInternalServerError should provide proper error message`() {
        val exception = sdkExceptionForInternalServerError()
        exception.message shouldBeEqualTo "Internal server error"
    }

    @Test
    fun `sdkExceptionForNoActivityContext should provide proper error message`() {
        val exception = sdkExceptionForNoActivityContext()
        exception.message shouldBeEqualTo "Only accept context of type Activity or ActivityCompat"
    }

    @Test
    fun `sdkExceptionForInvalidArguments should provide proper error message when blank`() {
        val exception = sdkExceptionForInvalidArguments()
        exception.message shouldBeEqualTo "Invalid arguments"
    }

    @Test
    fun `sdkExceptionForInvalidArguments should provide proper error message`() {
        val exception = sdkExceptionForInvalidArguments("message")
        exception.message shouldBeEqualTo "Invalid arguments: message"
    }
}
