//[miniapp](../../../../index.md)/[com.rakuten.tech.mobile.miniapp.view](../../index.md)/[MiniAppParameters](../index.md)/[DefaultParams](index.md)/[DefaultParams](-default-params.md)

# DefaultParams

[androidJvm]\
fun [DefaultParams](-default-params.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), config: [MiniAppConfig](../../-mini-app-config/index.md), miniAppId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), miniAppVersion: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), fromCache: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false, fromBundle: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false)

## Parameters

androidJvm

| | |
|---|---|
| context | is used by the view for initializing the internal services. Must be the context of activity to ensure that all standard html components work properly. |
| config | provide the necessary configuration to provide an independent MiniApp. |
| miniAppId | mini app id. |
| fromCache | allow host app to load miniapp from cache. |
