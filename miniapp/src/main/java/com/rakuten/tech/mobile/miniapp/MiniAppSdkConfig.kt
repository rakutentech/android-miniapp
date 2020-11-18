package com.rakuten.tech.mobile.miniapp

/**
 * This represents the configuration settings for the Mini App SDK.
 * @property baseUrl Base URL used for retrieving a Mini App.
 * @property rasAppId App ID for the Platform API.
 * @property subscriptionKey Subscription Key for the Platform API.
 * @property hostAppUserAgentInfo User Agent information from Host App.
 * @property isPreviewMode Whether the host app wants to use the API Endpoints under "Preview" mode.
 */
data class MiniAppSdkConfig(
    val baseUrl: String,
    val rasAppId: String,
    val subscriptionKey: String,
    val hostAppUserAgentInfo: String,
    val isPreviewMode: Boolean
) {
    internal val key = "$baseUrl-$isPreviewMode-$rasAppId-$subscriptionKey"

    init {
        when {
            !isBaseUrlValid(baseUrl) ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid baseUrl")
            rasAppId.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid rasAppId")
            subscriptionKey.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid subscriptionKey")
        }
    }
}

private fun isBaseUrlValid(url: String) = url.startsWith("https://")
