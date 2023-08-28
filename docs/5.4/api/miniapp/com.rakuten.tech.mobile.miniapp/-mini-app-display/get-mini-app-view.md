//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniAppDisplay](index.md)/[getMiniAppView](get-mini-app-view.md)

# getMiniAppView

[androidJvm]\
abstract suspend fun [getMiniAppView](get-mini-app-view.md)(activityContext: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [View](https://developer.android.com/reference/kotlin/android/view/View.html)?

Provides the view associated with the mini app to the caller for showing the mini app.

#### Return

[View](https://developer.android.com/reference/kotlin/android/view/View.html) as mini app's view with LayoutParams set to match the parent's dimensions.

## Parameters

androidJvm

| | |
|---|---|
| activityContext | is used by the view for initializing the internal services. Must be the context of activity to ensure that all standard html components work properly. |

## Throws

| | |
|---|---|
| [com.rakuten.tech.mobile.miniapp.MiniAppSdkException](../-mini-app-sdk-exception/index.md) | when a non-matching context is supplied |
