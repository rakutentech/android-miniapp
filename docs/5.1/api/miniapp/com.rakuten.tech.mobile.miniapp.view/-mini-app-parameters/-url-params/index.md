//[miniapp](../../../../index.md)/[com.rakuten.tech.mobile.miniapp.view](../../index.md)/[MiniAppParameters](../index.md)/[UrlParams](index.md)

# UrlParams

[androidJvm]\
data class [UrlParams](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), config: [MiniAppConfig](../../-mini-app-config/index.md), miniAppUrl: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [MiniAppParameters](../index.md)

This class can be used in the HostApp to create the mini app views using provided url.

## Parameters

androidJvm

| | |
|---|---|
| context | is used by the view for initializing the internal services. Must be the context of activity to ensure that all standard html components work properly. |
| config | provide the necessary configuration to provide an independent MiniApp. Mini app is NOT downloaded and cached in local, its content are read directly from the url. This should only be used for previewing a mini app from a local server. |
| miniAppUrl | a HTTP url containing Mini App content. |

## Constructors

| | |
|---|---|
| [UrlParams](-url-params.md) | [androidJvm]<br>fun [UrlParams](-url-params.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), config: [MiniAppConfig](../../-mini-app-config/index.md), miniAppUrl: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [config](config.md) | [androidJvm]<br>val [config](config.md): [MiniAppConfig](../../-mini-app-config/index.md) |
| [context](context.md) | [androidJvm]<br>val [context](context.md): [Context](https://developer.android.com/reference/kotlin/android/content/Context.html) |
| [miniAppUrl](mini-app-url.md) | [androidJvm]<br>val [miniAppUrl](mini-app-url.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
