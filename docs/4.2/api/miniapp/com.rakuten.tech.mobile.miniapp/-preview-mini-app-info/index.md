//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[PreviewMiniAppInfo](index.md)

# PreviewMiniAppInfo

[androidJvm]\
data class [PreviewMiniAppInfo](index.md)(host: [Host](../-host/index.md), miniapp: [MiniAppInfo](../-mini-app-info/index.md)) : [Parcelable](https://developer.android.com/reference/kotlin/android/os/Parcelable.html)

This represents a response entity for preview code.

## Constructors

| | |
|---|---|
| [PreviewMiniAppInfo](-preview-mini-app-info.md) | [androidJvm]<br>fun [PreviewMiniAppInfo](-preview-mini-app-info.md)(host: [Host](../-host/index.md), miniapp: [MiniAppInfo](../-mini-app-info/index.md)) |

## Functions

| Name | Summary |
|---|---|
| [describeContents](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1578325224%2FFunctions%2F1451286739) | [androidJvm]<br>abstract fun [describeContents](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1578325224%2FFunctions%2F1451286739)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [writeToParcel](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1754457655%2FFunctions%2F1451286739) | [androidJvm]<br>abstract fun [writeToParcel](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1754457655%2FFunctions%2F1451286739)(p0: [Parcel](https://developer.android.com/reference/kotlin/android/os/Parcel.html), p1: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [host](host.md) | [androidJvm]<br>@SerializedName(value = "host")<br>val [host](host.md): [Host](../-host/index.md)<br>host identifier unique to a mini app. |
| [miniapp](miniapp.md) | [androidJvm]<br>@SerializedName(value = "miniapp")<br>val [miniapp](miniapp.md): [MiniAppInfo](../-mini-app-info/index.md)<br>represents a Mini App entity. |
