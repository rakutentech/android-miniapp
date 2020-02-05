[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniAppView](./index.md)

# MiniAppView

`interface MiniAppView`

This represents the contract by which a mini app is rendered to display.

### Functions

| [destroyView](destroy-view.md) | Destroys the WebView associated to the mini app.`abstract fun destroyView(webView: `[`WebView`](https://developer.android.com/reference/android/webkit/WebView.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [obtainView](obtain-view.md) | Provides the actual view for a mini app to the caller. The caller must provide a valid [Context](https://developer.android.com/reference/android/content/Context.html) object (used to access application assets).`abstract suspend fun obtainView(activityContext: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`): `[`WebView`](https://developer.android.com/reference/android/webkit/WebView.html) |

