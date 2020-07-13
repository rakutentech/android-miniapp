package com.rakuten.tech.mobile.miniapp

/**
 * This represents the configuration settings for the Mini App SDK.
 * @property baseUrl Base URL used for retrieving a Mini App.
 * @property isTestMode Whether the sdk is running in Testing mode.
 * @property rasAppId App ID for the Platform API.
 * @property subscriptionKey Subscription Key for the Platform API.
 * @property hostAppVersionId Version of the host app, used to determine feature compatibility for Mini App.
 */
data class MiniAppSdkConfig(
    val baseUrl: String,
    val rasAppId: String,
    val subscriptionKey: String,
    val hostAppVersionId: String,
    val hostAppInfo: String,
    val isTestMode: Boolean
) {
    internal val key = "$baseUrl-$isTestMode-$rasAppId-$subscriptionKey-$hostAppVersionId"

    init {
        when {
            !isBaseUrlValid(baseUrl) ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid baseUrl")
            rasAppId.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid rasAppId")
            subscriptionKey.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid subscriptionKey")
            hostAppVersionId.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid hostAppVersionId")
        }
    }
}

private fun isBaseUrlValid(url: String) = url.startsWith("https://")
