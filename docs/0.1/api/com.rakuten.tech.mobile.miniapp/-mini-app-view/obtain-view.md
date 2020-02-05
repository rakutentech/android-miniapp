[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniAppView](index.md) / [obtainView](./obtain-view.md)

# obtainView

`abstract suspend fun obtainView(activityContext: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`): `[`WebView`](https://developer.android.com/reference/android/webkit/WebView.html)

Provides the actual view for a mini app to the caller.
The caller must provide a valid [Context](https://developer.android.com/reference/android/content/Context.html) object (used to access application assets).

**Return**
[WebView](https://developer.android.com/reference/android/webkit/WebView.html) as mini app's view.

