[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.navigator](../index.md) / [MiniAppExternalUrlLoader](./index.md)

# MiniAppExternalUrlLoader

`class MiniAppExternalUrlLoader`

This support the scenario that external loader redirect to url which is only supported in mini app view,
close the external loader and emit that url to mini app view by [ExternalResultHandler.emitResult](../-external-result-handler/emit-result.md).

### Functions

| [shouldClose](should-close.md) | In case you do not want to finish activity which contains webview automatically, use this to check should stop the external webview loader and send the current url to mini app view.`fun shouldClose(url: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [shouldOverrideUrlLoading](should-override-url-loading.md) | Determine to close the external loader. Use this in the return value of [WebViewClient.shouldOverrideUrlLoadingWebView,WebResourceRequest](#).`fun shouldOverrideUrlLoading(url: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

### Companion Object Properties

| [returnUrlTag](return-url-tag.md) | `const val returnUrlTag: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

### Companion Object Functions

| [loaderWithId](loader-with-id.md) | Creates new MiniAppExternalUrlLoader.`fun loaderWithId(miniAppId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, activity: `[`Activity`](https://developer.android.com/reference/android/app/Activity.html)`? = null): `[`MiniAppExternalUrlLoader`](./index.md) |
| [loaderWithUrl](loader-with-url.md) | Creates new MiniAppExternalUrlLoader. This should only be used for previewing a mini app from a local server.`fun loaderWithUrl(customAppUrl: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, activity: `[`Activity`](https://developer.android.com/reference/android/app/Activity.html)`? = null): `[`MiniAppExternalUrlLoader`](./index.md) |

