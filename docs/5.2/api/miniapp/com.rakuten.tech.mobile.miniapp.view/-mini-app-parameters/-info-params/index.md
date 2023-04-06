//[miniapp](../../../../index.md)/[com.rakuten.tech.mobile.miniapp.view](../../index.md)/[MiniAppParameters](../index.md)/[InfoParams](index.md)

# InfoParams

[androidJvm]\
data class [InfoParams](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), config: [MiniAppConfig](../../-mini-app-config/index.md), miniAppInfo: [MiniAppInfo](../../../com.rakuten.tech.mobile.miniapp/-mini-app-info/index.md), fromCache: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) : [MiniAppParameters](../index.md)

This class can be used in the HostApp to create the mini app views using mini app id.

## Parameters

androidJvm

| | |
|---|---|
| context | is used by the view for initializing the internal services. Must be the context of activity to ensure that all standard html components work properly. |
| config | provide the necessary configuration to provide an independent MiniApp. |
| miniAppInfo | metadata of a mini app. |
| fromCache | allow host app to load miniapp from cache. |

## Constructors

| | |
|---|---|
| [InfoParams](-info-params.md) | [androidJvm]<br>fun [InfoParams](-info-params.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), config: [MiniAppConfig](../../-mini-app-config/index.md), miniAppInfo: [MiniAppInfo](../../../com.rakuten.tech.mobile.miniapp/-mini-app-info/index.md), fromCache: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false) |

## Properties

| Name | Summary |
|---|---|
| [config](config.md) | [androidJvm]<br>val [config](config.md): [MiniAppConfig](../../-mini-app-config/index.md) |
| [context](context.md) | [androidJvm]<br>val [context](context.md): [Context](https://developer.android.com/reference/kotlin/android/content/Context.html) |
| [fromCache](from-cache.md) | [androidJvm]<br>var [fromCache](from-cache.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false |
| [miniAppInfo](mini-app-info.md) | [androidJvm]<br>val [miniAppInfo](mini-app-info.md): [MiniAppInfo](../../../com.rakuten.tech.mobile.miniapp/-mini-app-info/index.md) |
