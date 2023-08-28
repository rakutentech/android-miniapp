//[miniapp](../../index.md)/[com.rakuten.tech.mobile.miniapp.analytics](index.md)

# Package com.rakuten.tech.mobile.miniapp.analytics

## Types

| Name | Summary |
|---|---|
| [MAAnalyticsActionType](-m-a-analytics-action-type/index.md) | [androidJvm]<br>enum [MAAnalyticsActionType](-m-a-analytics-action-type/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[MAAnalyticsActionType](-m-a-analytics-action-type/index.md)&gt; <br>Action Type. |
| [MAAnalyticsEventType](-m-a-analytics-event-type/index.md) | [androidJvm]<br>enum [MAAnalyticsEventType](-m-a-analytics-event-type/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[MAAnalyticsEventType](-m-a-analytics-event-type/index.md)&gt; <br>Event Type. |
| [MAAnalyticsInfo](-m-a-analytics-info/index.md) | [androidJvm]<br>data class [MAAnalyticsInfo](-m-a-analytics-info/index.md)(eventType: [MAAnalyticsEventType](-m-a-analytics-event-type/index.md), actionType: [MAAnalyticsActionType](-m-a-analytics-action-type/index.md), pageName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), componentName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), elementType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), data: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))<br>Mini App Analytics info type. |
| [MiniAppAnalyticsConfig](-mini-app-analytics-config/index.md) | [androidJvm]<br>data class [MiniAppAnalyticsConfig](-mini-app-analytics-config/index.md)(acc: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), aid: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [Parcelable](https://developer.android.com/reference/kotlin/android/os/Parcelable.html)<br>Contains the components which need to add extra analytics credentials from host app. |
