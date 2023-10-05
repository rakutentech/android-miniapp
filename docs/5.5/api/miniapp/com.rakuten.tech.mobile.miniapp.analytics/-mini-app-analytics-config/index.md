//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.analytics](../index.md)/[MiniAppAnalyticsConfig](index.md)

# MiniAppAnalyticsConfig

[androidJvm]\
data class [MiniAppAnalyticsConfig](index.md)(acc: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), aid: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [Parcelable](https://developer.android.com/reference/kotlin/android/os/Parcelable.html)

Contains the components which need to add extra analytics credentials from host app.

## Constructors

| | |
|---|---|
| [MiniAppAnalyticsConfig](-mini-app-analytics-config.md) | [androidJvm]<br>fun [MiniAppAnalyticsConfig](-mini-app-analytics-config.md)(acc: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), aid: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Functions

| Name | Summary |
|---|---|
| [describeContents](index.md#-1578325224%2FFunctions%2F1451286739) | [androidJvm]<br>abstract fun [describeContents](index.md#-1578325224%2FFunctions%2F1451286739)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [writeToParcel](index.md#-1754457655%2FFunctions%2F1451286739) | [androidJvm]<br>abstract fun [writeToParcel](index.md#-1754457655%2FFunctions%2F1451286739)(p0: [Parcel](https://developer.android.com/reference/kotlin/android/os/Parcel.html), p1: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [acc](acc.md) | [androidJvm]<br>val [acc](acc.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The RAT account id. |
| [aid](aid.md) | [androidJvm]<br>val [aid](aid.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The RAT app id. |
