package com.rakuten.tech.mobile.miniapp

import org.amshove.kluent.shouldEqual
import org.junit.Test

class MiniAppSdkConfigSpec {

    @Test
    fun `the key pattern should match the defined scheme`() {
        val config = MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isPreviewMode = true,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
        config.key shouldEqual "${config.baseUrl}-${config.isPreviewMode}" +
                "-${config.rasProjectId}-${config.subscriptionKey}"
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when api url is not https`() {
        MiniAppSdkConfig(
            baseUrl = "http://www.example.com/1",
            isPreviewMode = false,
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
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = " ",
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }
}
