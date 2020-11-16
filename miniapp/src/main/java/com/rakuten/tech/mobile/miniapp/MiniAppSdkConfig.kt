package com.rakuten.tech.mobile.miniapp

/**
 * This represents the configuration settings for the Mini App SDK.
 * @property baseUrl Base URL used for retrieving a Mini App.
 * @property rasProjectId Project ID for the Platform API.
 * @property rasAppId App ID for the Platform API (Deprecated).
 * @property subscriptionKey Subscription Key for the Platform API.
 * @property hostAppVersionId Version of the host app, used to determine feature compatibility for Mini App.
 * @property hostAppUserAgentInfo User Agent information from Host App.
 * @property isTestMode Whether the sdk is making use of the Test API Endpoints for under "Testing" mini apps.
 */
data class MiniAppSdkConfig(
    val baseUrl: String,
    var rasProjectId: String,
    @Deprecated("Use rasProjectId") var rasAppId: String,
    val subscriptionKey: String,
    val hostAppVersionId: String,
    val hostAppUserAgentInfo: String,
    val isTestMode: Boolean
) {
    internal var projectOrAppId = ""

    init {
        when {
            !isBaseUrlValid(baseUrl) ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid baseUrl")
            subscriptionKey.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid subscriptionKey")
            hostAppVersionId.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid hostAppVersionId")
            rasProjectId.isBlank() && rasAppId.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid either rasProjectId or rasAppId")
            rasProjectId.isNotBlank() && rasAppId.isNotBlank() ->
                projectOrAppId = rasProjectId // Project ID has the first priority
            rasProjectId.isBlank() && rasAppId.isNotBlank() ->
                projectOrAppId = rasAppId
            rasProjectId.isNotBlank() && rasAppId.isBlank() ->
                projectOrAppId = rasProjectId
        }
    }

    internal val key = "$baseUrl-$isTestMode-$projectOrAppId-$subscriptionKey-$hostAppVersionId"
}

private fun isBaseUrlValid(url: String) = url.startsWith("https://")
