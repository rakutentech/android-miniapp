package com.rakuten.tech.mobile.miniapp

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.Test

class MiniAppSdkConfigSpec {

    @Test
    fun `the key pattern should match the defined scheme`() {
        val config = MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            testUrl = TEST_URL_HTTPS_2,
            isTestMode = true,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION
        )
        config.key shouldEqual
                "${config.providedUrl}-${config.rasAppId}-${config.subscriptionKey}-${config.hostAppVersionId}"
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when api url is not https`() {
        MiniAppSdkConfig(
            baseUrl = "http://www.example.com/1",
            testUrl = TEST_URL_HTTPS_2,
            isTestMode = false,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when api url is blank`() {
        MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            testUrl = " ",
            isTestMode = true,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when test api is null in test mode`() {
        MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            testUrl = null,
            isTestMode = true,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when rasAppId is blank`() {
        MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            testUrl = TEST_URL_HTTPS_2,
            isTestMode = true,
            rasAppId = " ",
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when subscriptionKey is blank`() {
        MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            testUrl = TEST_URL_HTTPS_2,
            isTestMode = true,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = " ",
            hostAppVersionId = TEST_HA_ID_VERSION
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when hostAppVersionId is blank`() {
        MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            testUrl = TEST_URL_HTTPS_2,
            isTestMode = true,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = " "
        )
    }

    @Suppress("LongMethod")
    @Test
    fun `should providing correct url type`() {
        val regularConfig = MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            testUrl = "$TEST_URL_HTTPS_2/test/",
            isTestMode = false,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION
        )
        regularConfig.isTestMode shouldBe false
        regularConfig.providedUrl shouldBe TEST_URL_HTTPS_2

        val testConfig = MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            testUrl = "$TEST_URL_HTTPS_2/test/",
            isTestMode = true,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION
        )
        testConfig.isTestMode shouldBe true
        testConfig.providedUrl shouldBe "$TEST_URL_HTTPS_2/test/"
    }
}
