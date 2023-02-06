//[miniapp](../../../../index.md)/[com.rakuten.tech.mobile.miniapp.navigator](../../index.md)/[MiniAppExternalUrlLoader](../index.md)/[Companion](index.md)/[loaderWithUrl](loader-with-url.md)

# loaderWithUrl

[androidJvm]\
fun [loaderWithUrl](loader-with-url.md)(customAppUrl: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), activity: [Activity](https://developer.android.com/reference/kotlin/android/app/Activity.html)? = null): [MiniAppExternalUrlLoader](../index.md)

Creates new MiniAppExternalUrlLoader. This should only be used for previewing a mini app from a local server.

## Parameters

androidJvm

| | |
|---|---|
| customAppUrl | The url that was used to load the Mini App. |
| activity | The Activity contains webview. Pass the activity if you want to auto finish the Activity with current external loading url as result data. |
