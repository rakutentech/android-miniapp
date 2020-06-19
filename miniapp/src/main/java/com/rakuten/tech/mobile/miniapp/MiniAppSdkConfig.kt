package com.rakuten.tech.mobile.miniapp

/**
 * This represents the configuration settings for the Mini App SDK.
 * @property baseUrl Url endpoint for the published mini apps.
 * @property testUrl Url endpoint for the testing mini apps.
 * @property isTestMode Whether the sdk is running in Testing mode.
 * @property rasAppId App ID for the Platform API.
 * @property subscriptionKey Subscription Key for the Platform API.
 * @property hostAppVersionId Version of the host app, used to determine feature compatibility for Mini App.
 */
data class MiniAppSdkConfig(
    private var baseUrl: String,
    private var testUrl: String?,
    var isTestMode: Boolean,
    var rasAppId: String,
    var subscriptionKey: String,
    var hostAppVersionId: String
) {
    internal val providedUrl: String? = if (isTestMode) testUrl else baseUrl
    internal val key = "$providedUrl-$rasAppId-$subscriptionKey-$hostAppVersionId"

    init {
        when {
            !isBaseUrlValid(providedUrl) ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid url endpoint")
            rasAppId.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid rasAppId")
            subscriptionKey.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid subscriptionKey")
            hostAppVersionId.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid hostAppVersionId")
        }
    }
}

private fun isBaseUrlValid(url: String?) = url != null && url.startsWith("https://")
