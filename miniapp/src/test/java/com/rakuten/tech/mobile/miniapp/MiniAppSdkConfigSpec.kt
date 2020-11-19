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
            hostAppVersionId = TEST_HA_ID_VERSION,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
        config.key shouldEqual "${config.baseUrl}-${config.isPreviewMode}" +
                "-${config.rasProjectId}-${config.subscriptionKey}-${config.hostAppVersionId}"
    }

    @Test
    fun `the key pattern should match the defined scheme when both project and app id have value`() {
        val config = MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isTestMode = true,
            rasProjectId = TEST_HA_ID_PROJECT,
            isPreviewMode = true,
            rasAppId = TEST_HA_ID_APP,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
        config.key shouldEqual "${config.baseUrl}-${config.isPreviewMode}" +
                "-${config.rasProjectId}-${config.subscriptionKey}-${config.hostAppVersionId}"
    }

    @Test
    fun `the key pattern should match the defined scheme when only project id has value`() {
        val config = MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isTestMode = true,
            rasProjectId = TEST_HA_ID_PROJECT,
            rasAppId = " ",
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
        config.key shouldEqual "${config.baseUrl}-${config.isPreviewMode}" +
                "-${config.rasProjectId}-${config.subscriptionKey}-${config.hostAppVersionId}"
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when api url is not https`() {
        MiniAppSdkConfig(
            baseUrl = "http://www.example.com/1",
            isPreviewMode = false,
            rasAppId = TEST_HA_ID_APP,
            isTestMode = false,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when api url is blank`() {
        MiniAppSdkConfig(
            baseUrl = " ",
            isPreviewMode = true,
            rasAppId = TEST_HA_ID_APP,
            isTestMode = true,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION,
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
            hostAppVersionId = TEST_HA_ID_VERSION,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when both projectId and rasAppId are blank`() {
        MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isPreviewMode = true,
            isTestMode = true,
            rasProjectId = " ",
            rasAppId = " ",
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = TEST_HA_ID_VERSION,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when subscriptionKey is blank`() {
        MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isPreviewMode = true,
            rasAppId = TEST_HA_ID_APP,
            isTestMode = true,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = " ",
            hostAppVersionId = TEST_HA_ID_VERSION,
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }

    @Test(expected = MiniAppSdkException::class)
    fun `should throw exception when hostAppVersionId is blank`() {
        MiniAppSdkConfig(
            baseUrl = TEST_URL_HTTPS_2,
            isPreviewMode = true,
            rasAppId = TEST_HA_ID_APP,
            isTestMode = true,
            rasProjectId = TEST_HA_ID_PROJECT,
            subscriptionKey = TEST_HA_SUBSCRIPTION_KEY,
            hostAppVersionId = " ",
            hostAppUserAgentInfo = TEST_HA_NAME
        )
    }
}
