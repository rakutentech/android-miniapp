package com.rakuten.tech.mobile.miniapp

import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.junit.Test

class MiniAppSdkConfigSpec {

    @Test
    fun `the key pattern should match the defined scheme`() {
        val config = MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isPreviewMode = true,
            requireSignatureVerification = true,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
        config.key shouldBeEqualTo "${config.baseUrl}-${config.isPreviewMode}" +
                "-${config.rasProjectId}-${config.subscriptionKey}"
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when api url is not https`() {
        MiniAppSdkConfig(
            baseUrl = "http://www.example.com/1",
            isPreviewMode = false,
            requireSignatureVerification = true,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when api url is blank`() {
        MiniAppSdkConfig(
            baseUrl = " ",
            isPreviewMode = true,
            requireSignatureVerification = true,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when projectId is blank`() {
        MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isPreviewMode = true,
            requireSignatureVerification = true,
            rasProjectId = " ",
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when subscriptionKey is blank`() {
        MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isPreviewMode = true,
            requireSignatureVerification = true,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = " ",
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }

    @Test
    fun `hostAppVersionId should contains empty value as default`() {
        val miniAppSdkConfig = MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isPreviewMode = true,
            requireSignatureVerification = true,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
        miniAppSdkConfig.hostAppVersionId.shouldBeEmpty()
    }

    @Test
    fun `requireSignatureVerification should be false as default`() {
        val miniAppSdkConfig = MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isPreviewMode = true,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
        miniAppSdkConfig.requireSignatureVerification.shouldBeFalse()
    }
}
