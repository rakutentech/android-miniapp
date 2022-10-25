//[miniapp](../../../../index.md)/[com.rakuten.tech.mobile.miniapp.view](../../index.md)/[MiniAppParameters](../index.md)/[UrlParams](index.md)/[UrlParams](-url-params.md)

# UrlParams

[androidJvm]\
fun [UrlParams](-url-params.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), config: [MiniAppConfig](../../-mini-app-config/index.md), miniAppUrl: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))

## Parameters

androidJvm

| | |
|---|---|
| context | is used by the view for initializing the internal services. Must be the context of activity to ensure that all standard html components work properly. |
| config | provide the necessary configuration to provide an independent MiniApp. Mini app is NOT downloaded and cached in local, its content are read directly from the url. This should only be used for previewing a mini app from a local server. |
| miniAppUrl | a HTTP url containing Mini App content. |
