[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniAppDisplay](index.md) / [getMiniAppView](./get-mini-app-view.md)

# getMiniAppView

`abstract suspend fun getMiniAppView(activityContext: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`): `[`View`](https://developer.android.com/reference/android/view/View.html)`?`

Provides the view associated with the mini app to the caller for showing the mini app.

### Parameters

`activityContext` - is used by the view for initializing the internal services.
Should be the context of activity to ensure that all standard html components work properly.

### Exceptions

`MiniAppSdkException` - when a non-matching context is supplied

**Return**
[View](https://developer.android.com/reference/android/view/View.html) as mini app's view with [LayoutParams](#) set to match
the parent's dimensions.

