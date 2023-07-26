//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.view](../index.md)/[MiniAppView](index.md)

# MiniAppView

[androidJvm]\
abstract class [MiniAppView](index.md)

This class can be used in the HostApp to create the miniapp views independently.

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [load](load.md) | [androidJvm]<br>abstract fun [load](load.md)(queryParams: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = "", fromCache: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false, onComplete: ([MiniAppDisplay](../../com.rakuten.tech.mobile.miniapp/-mini-app-display/index.md)?, [MiniAppSdkException](../../com.rakuten.tech.mobile.miniapp/-mini-app-sdk-exception/index.md)?) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>load a mini app view. The mini app is downloaded, saved and provides a view when successful. |
| [loadFromBundle](load-from-bundle.md) | [androidJvm]<br>abstract fun [loadFromBundle](load-from-bundle.md)(onComplete: ([MiniAppDisplay](../../com.rakuten.tech.mobile.miniapp/-mini-app-display/index.md)?, [MiniAppSdkException](../../com.rakuten.tech.mobile.miniapp/-mini-app-sdk-exception/index.md)?) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>load a mini app view from bundle. The mini app is downloaded, saved and provides a view when successful. |
| [sendJsonToMiniApp](send-json-to-mini-app.md) | [androidJvm]<br>abstract fun [sendJsonToMiniApp](send-json-to-mini-app.md)(message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), onFailed: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>Send a generic message to MiniApp using [com.rakuten.tech.mobile.miniapp.js.NativeEventType.MINIAPP_RECEIVE_JSON_INFO](../../com.rakuten.tech.mobile.miniapp.js/-native-event-type/-m-i-n-i-a-p-p_-r-e-c-e-i-v-e_-j-s-o-n_-i-n-f-o/index.md). |
