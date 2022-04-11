//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.navigator](../index.md)/[MiniAppExternalUrlLoader](index.md)

# MiniAppExternalUrlLoader

[androidJvm]\
class [MiniAppExternalUrlLoader](index.md)

This support the scenario that external loader redirect to url which is only supported in mini app view, close the external loader and emit that url to mini app view by [ExternalResultHandler.emitResult](../-external-result-handler/emit-result.md).

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [shouldClose](should-close.md) | [androidJvm]<br>fun [shouldClose](should-close.md)(url: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>In case you do not want to finish activity which contains webview automatically, use this to check should stop the external webview loader and send the current url to mini app view. |
| [shouldOverrideUrlLoading](should-override-url-loading.md) | [androidJvm]<br>fun [shouldOverrideUrlLoading](should-override-url-loading.md)(url: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Determine to close the external loader. Use this in the return value of WebViewClient.shouldOverrideUrlLoading(WebView, WebResourceRequest). |
