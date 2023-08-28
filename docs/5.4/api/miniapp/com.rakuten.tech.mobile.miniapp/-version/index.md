//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[Version](index.md)

# Version

[androidJvm]\
data class [Version](index.md)(versionTag: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), versionId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [Parcelable](https://developer.android.com/reference/kotlin/android/os/Parcelable.html)

This represents a version entity of a Mini App.

## Constructors

| | |
|---|---|
| [Version](-version.md) | [androidJvm]<br>fun [Version](-version.md)(versionTag: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), versionId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |

## Functions

| Name | Summary |
|---|---|
| [describeContents](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1578325224%2FFunctions%2F1451286739) | [androidJvm]<br>abstract fun [describeContents](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1578325224%2FFunctions%2F1451286739)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [writeToParcel](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1754457655%2FFunctions%2F1451286739) | [androidJvm]<br>abstract fun [writeToParcel](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1754457655%2FFunctions%2F1451286739)(p0: [Parcel](https://developer.android.com/reference/kotlin/android/os/Parcel.html), p1: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [versionId](version-id.md) | [androidJvm]<br>@SerializedName(value = "versionId")<br>val [versionId](version-id.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Version identifier of the mini app. |
| [versionTag](version-tag.md) | [androidJvm]<br>@SerializedName(value = "versionTag")<br>val [versionTag](version-tag.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Version information of the mini app. |
