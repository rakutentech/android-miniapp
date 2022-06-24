//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniAppSdkConfig](index.md)

# MiniAppSdkConfig

[androidJvm]\
data class [MiniAppSdkConfig](index.md)(baseUrl: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), rasProjectId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), subscriptionKey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), hostAppVersionId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), hostAppUserAgentInfo: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), isPreviewMode: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), requireSignatureVerification: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), miniAppAnalyticsConfigList: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[MiniAppAnalyticsConfig](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md)&gt;, sslPinningPublicKeyList: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;, enableH5Ads: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), storageMaxSizeKB: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [Parcelable](https://developer.android.com/reference/kotlin/android/os/Parcelable.html)

This represents the configuration settings for the Mini App SDK.

## Constructors

| | |
|---|---|
| [MiniAppSdkConfig](-mini-app-sdk-config.md) | [androidJvm]<br>fun [MiniAppSdkConfig](-mini-app-sdk-config.md)(baseUrl: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), rasProjectId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), subscriptionKey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), hostAppVersionId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = "", hostAppUserAgentInfo: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), isPreviewMode: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), requireSignatureVerification: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false, miniAppAnalyticsConfigList: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[MiniAppAnalyticsConfig](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md)&gt; = emptyList(), sslPinningPublicKeyList: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt; = emptyList(), enableH5Ads: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false, storageMaxSizeKB: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 5000) |

## Functions

| Name | Summary |
|---|---|
| [describeContents](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1578325224%2FFunctions%2F1451286739) | [androidJvm]<br>abstract fun [describeContents](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1578325224%2FFunctions%2F1451286739)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [writeToParcel](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1754457655%2FFunctions%2F1451286739) | [androidJvm]<br>abstract fun [writeToParcel](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1754457655%2FFunctions%2F1451286739)(p0: [Parcel](https://developer.android.com/reference/kotlin/android/os/Parcel.html), p1: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [baseUrl](base-url.md) | [androidJvm]<br>val [baseUrl](base-url.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Base URL used for retrieving a Mini App. |
| [enableH5Ads](enable-h5-ads.md) | [androidJvm]<br>val [enableH5Ads](enable-h5-ads.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false<br>Whether the host app wants to enable ad placement beta. |
| [hostAppUserAgentInfo](host-app-user-agent-info.md) | [androidJvm]<br>val [hostAppUserAgentInfo](host-app-user-agent-info.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>User Agent information from Host App. |
| [hostAppVersionId](host-app-version-id.md) | [androidJvm]<br>val [hostAppVersionId](host-app-version-id.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Version of the host app, used to determine feature compatibility for Mini App. |
| [isPreviewMode](is-preview-mode.md) | [androidJvm]<br>val [isPreviewMode](is-preview-mode.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Whether the host app wants to use the API Endpoints under "Preview" mode. |
| [miniAppAnalyticsConfigList](mini-app-analytics-config-list.md) | [androidJvm]<br>val [miniAppAnalyticsConfigList](mini-app-analytics-config-list.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[MiniAppAnalyticsConfig](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md)&gt;<br>List of analytic config to send events on. |
| [rasProjectId](ras-project-id.md) | [androidJvm]<br>val [rasProjectId](ras-project-id.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Project ID for the Platform API. |
| [requireSignatureVerification](require-signature-verification.md) | [androidJvm]<br>val [requireSignatureVerification](require-signature-verification.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false<br>Whether the Mini App SDK verifies signature of a Mini App. |
| [sslPinningPublicKeyList](ssl-pinning-public-key-list.md) | [androidJvm]<br>val [sslPinningPublicKeyList](ssl-pinning-public-key-list.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;<br>List of SSL pinning public keys. |
| [storageMaxSizeKB](storage-max-size-k-b.md) | [androidJvm]<br>val [storageMaxSizeKB](storage-max-size-k-b.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 5000<br>Maximum size in KB for each Mini App is allowed to use for Secure Storage, Default is 5MB. |
| [subscriptionKey](subscription-key.md) | [androidJvm]<br>val [subscriptionKey](subscription-key.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Subscription Key for the Platform API. |
