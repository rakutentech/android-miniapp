//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[Host](index.md)

# Host

[androidJvm]\
data class [Host](index.md)(id: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), subscriptionKey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [Parcelable](https://developer.android.com/reference/kotlin/android/os/Parcelable.html)

This represents a host entity of a Mini App.

## Constructors

| | |
|---|---|
| [Host](-host.md) | [androidJvm]<br>fun [Host](-host.md)(id: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), subscriptionKey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |

## Functions

| Name | Summary |
|---|---|
| [describeContents](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1578325224%2FFunctions%2F1451286739) | [androidJvm]<br>abstract fun [describeContents](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1578325224%2FFunctions%2F1451286739)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [writeToParcel](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1754457655%2FFunctions%2F1451286739) | [androidJvm]<br>abstract fun [writeToParcel](../../com.rakuten.tech.mobile.miniapp.analytics/-mini-app-analytics-config/index.md#-1754457655%2FFunctions%2F1451286739)(p0: [Parcel](https://developer.android.com/reference/kotlin/android/os/Parcel.html), p1: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [id](id.md) | [androidJvm]<br>@SerializedName(value = "id")<br>val [id](id.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>host id information of the mini app. |
| [subscriptionKey](subscription-key.md) | [androidJvm]<br>@SerializedName(value = "subscriptionKey")<br>val [subscriptionKey](subscription-key.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>subscription identifier of the mini app. |
