//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniAppInfo](index.md)

# MiniAppInfo

[androidJvm]\
data class [MiniAppInfo](index.md)(id: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), displayName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), icon: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), version: [Version](../-version/index.md), promotionalImageUrl: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, promotionalText: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) : [Parcelable](https://developer.android.com/reference/kotlin/android/os/Parcelable.html)

This represents a Mini App entity.

## Constructors

| | |
|---|---|
| [MiniAppInfo](-mini-app-info.md) | [androidJvm]<br>fun [MiniAppInfo](-mini-app-info.md)(id: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), displayName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), icon: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), version: [Version](../-version/index.md), promotionalImageUrl: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, promotionalText: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [describeContents](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1578325224%2FFunctions%2F1451286739) | [androidJvm]<br>abstract fun [describeContents](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1578325224%2FFunctions%2F1451286739)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [writeToParcel](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1754457655%2FFunctions%2F1451286739) | [androidJvm]<br>abstract fun [writeToParcel](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1754457655%2FFunctions%2F1451286739)(p0: [Parcel](https://developer.android.com/reference/kotlin/android/os/Parcel.html), p1: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [displayName](display-name.md) | [androidJvm]<br>@SerializedName(value = "displayName")<br>val [displayName](display-name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Display name of the mini app. |
| [icon](icon.md) | [androidJvm]<br>@SerializedName(value = "icon")<br>val [icon](icon.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Icon of the mini app, obtainable from the provided data for this resource. |
| [id](id.md) | [androidJvm]<br>@SerializedName(value = "id")<br>val [id](id.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Mini App identifier unique to a mini app. |
| [promotionalImageUrl](promotional-image-url.md) | [androidJvm]<br>@SerializedName(value = "promotionalImageUrl")<br>val [promotionalImageUrl](promotional-image-url.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>promotional image, obtainable from the provided data for this resource. |
| [promotionalText](promotional-text.md) | [androidJvm]<br>@SerializedName(value = "promotionalText")<br>val [promotionalText](promotional-text.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>promotional details to share. |
| [version](version.md) | [androidJvm]<br>@SerializedName(value = "version")<br>val [version](version.md): [Version](../-version/index.md)<br>Version information of the mini app. |
