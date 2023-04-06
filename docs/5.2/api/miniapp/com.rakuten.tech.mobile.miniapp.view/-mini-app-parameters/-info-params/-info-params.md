//[miniapp](../../../../index.md)/[com.rakuten.tech.mobile.miniapp.view](../../index.md)/[MiniAppParameters](../index.md)/[InfoParams](index.md)/[InfoParams](-info-params.md)

# InfoParams

[androidJvm]\
fun [InfoParams](-info-params.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), config: [MiniAppConfig](../../-mini-app-config/index.md), miniAppInfo: [MiniAppInfo](../../../com.rakuten.tech.mobile.miniapp/-mini-app-info/index.md), fromCache: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false)

## Parameters

androidJvm

| | |
|---|---|
| context | is used by the view for initializing the internal services. Must be the context of activity to ensure that all standard html components work properly. |
| config | provide the necessary configuration to provide an independent MiniApp. |
| miniAppInfo | metadata of a mini app. |
| fromCache | allow host app to load miniapp from cache. |
