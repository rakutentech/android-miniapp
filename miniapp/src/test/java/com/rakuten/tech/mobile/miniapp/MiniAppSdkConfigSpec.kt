package com.rakuten.tech.mobile.miniapp

import org.amshove.kluent.shouldEqual
import org.junit.Test

class MiniAppSdkConfigSpec {

    @Test
    fun `the key pattern should match the defined scheme`() {
        val config = MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isPreviewMode = true,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
        config.key shouldEqual "${config.baseUrl}-${config.isPreviewMode}" +
                "-${config.rasAppId}-${config.subscriptionKey}"
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when api url is not https`() {
        MiniAppSdkConfig(
            baseUrl = "http://www.example.com/1",
            isPreviewMode = false,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when api url is blank`() {
        MiniAppSdkConfig(
            baseUrl = " ",
            isPreviewMode = true,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when rasAppId is blank`() {
        MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isPreviewMode = true,
            rasAppId = " ",
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when subscriptionKey is blank`() {
        MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isPreviewMode = true,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = " ",
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }
}
