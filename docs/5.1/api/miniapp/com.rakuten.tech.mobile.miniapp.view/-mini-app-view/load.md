//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.view](../index.md)/[MiniAppView](index.md)/[load](load.md)

# load

[androidJvm]\
abstract fun [load](load.md)(queryParams: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = "", fromCache: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false, onComplete: ([MiniAppDisplay](../../com.rakuten.tech.mobile.miniapp/-mini-app-display/index.md)?, [MiniAppSdkException](../../com.rakuten.tech.mobile.miniapp/-mini-app-sdk-exception/index.md)?) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

load a mini app view. The mini app is downloaded, saved and provides a view when successful.

## Parameters

androidJvm

| | |
|---|---|
| queryParams | the parameters will be appended with the miniapp url scheme. |
| fromCache | the parameters will be appended with cached miniapp. |
| onComplete | parameters needed to callback when the miniapp is successfully loaded. |
