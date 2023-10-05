//[miniapp](../../../../index.md)/[com.rakuten.tech.mobile.miniapp.view](../../index.md)/[MiniAppParameters](../index.md)/[DefaultParams](index.md)

# DefaultParams

[androidJvm]\
data class [DefaultParams](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), config: [MiniAppConfig](../../-mini-app-config/index.md), miniAppId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), miniAppVersion: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), fromCache: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), fromBundle: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) : [MiniAppParameters](../index.md)

This class can be used in the HostApp to create the mini app views using mini app id.

## Parameters

androidJvm

| | |
|---|---|
| context | is used by the view for initializing the internal services. Must be the context of activity to ensure that all standard html components work properly. |
| config | provide the necessary configuration to provide an independent MiniApp. |
| miniAppId | mini app id. |
| fromCache | allow host app to load miniapp from cache. |

## Constructors

| | |
|---|---|
| [DefaultParams](-default-params.md) | [androidJvm]<br>fun [DefaultParams](-default-params.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), config: [MiniAppConfig](../../-mini-app-config/index.md), miniAppId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), miniAppVersion: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), fromCache: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false, fromBundle: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false) |

## Properties

| Name | Summary |
|---|---|
| [config](config.md) | [androidJvm]<br>val [config](config.md): [MiniAppConfig](../../-mini-app-config/index.md) |
| [context](context.md) | [androidJvm]<br>val [context](context.md): [Context](https://developer.android.com/reference/kotlin/android/content/Context.html) |
| [fromBundle](from-bundle.md) | [androidJvm]<br>var [fromBundle](from-bundle.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false |
| [fromCache](from-cache.md) | [androidJvm]<br>var [fromCache](from-cache.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false |
| [miniAppId](mini-app-id.md) | [androidJvm]<br>val [miniAppId](mini-app-id.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [miniAppVersion](mini-app-version.md) | [androidJvm]<br>val [miniAppVersion](mini-app-version.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
