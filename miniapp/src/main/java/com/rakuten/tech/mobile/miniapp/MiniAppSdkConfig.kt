package com.rakuten.tech.mobile.miniapp

import android.os.Parcelable
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalyticsConfig
import kotlinx.android.parcel.Parcelize

/**
 * This represents the configuration settings for the Mini App SDK.
 * @property baseUrl Base URL used for retrieving a Mini App.
 * @property rasProjectId Project ID for the Platform API.
 * @property subscriptionKey Subscription Key for the Platform API.
 * @property hostAppVersionId Version of the host app, used to determine feature compatibility for Mini App.
 * @property hostAppUserAgentInfo User Agent information from Host App.
 * @property isPreviewMode Whether the host app wants to use the API Endpoints under "Preview" mode.
 * @property requireSignatureVerification Whether the Mini App SDK verifies signature of a Mini App.
 * @property miniAppAnalyticsConfigList List of analytic config to send events on.
 * @property sslPinningPublicKeyList List of SSL pinning public keys.
 * @property enableH5Ads Whether the host app wants to enable ad placement beta.
 * @property maxStorageSizeLimitInBytes Maximum Secure Storage size limit in bytes for each Mini App, Default is 5MB.
 */
@Parcelize
data class MiniAppSdkConfig(
    val baseUrl: String,
    val rasProjectId: String,
    val subscriptionKey: String,
    val hostAppVersionId: String = "",
    val hostAppUserAgentInfo: String,
    val isPreviewMode: Boolean,
    val requireSignatureVerification: Boolean = false,
    val miniAppAnalyticsConfigList: List<MiniAppAnalyticsConfig> = emptyList(),
    val sslPinningPublicKeyList: List<String> = emptyList(),
    val enableH5Ads: Boolean = false,
    val maxStorageSizeLimitInBytes: String = "52428800" // Default max storage limit is 5 MB
) : Parcelable {
    internal val key = "$baseUrl-$isPreviewMode-$rasProjectId-$subscriptionKey"

    init {
        when {
            !isBaseUrlValid(baseUrl) ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid baseUrl")
            rasProjectId.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid rasProjectId")
            subscriptionKey.isBlank() ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid subscriptionKey")
        }
    }
}

private fun isBaseUrlValid(url: String) = url.startsWith("https://")
