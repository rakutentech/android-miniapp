[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.navigator](../index.md) / [MiniAppExternalUrlLoader](index.md) / [loaderWithId](./loader-with-id.md)

# loaderWithId

`fun loaderWithId(miniAppId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, activity: `[`Activity`](https://developer.android.com/reference/android/app/Activity.html)`? = null): `[`MiniAppExternalUrlLoader`](index.md)

Creates new MiniAppExternalUrlLoader.

### Parameters

`miniAppId` - The id of loading mini app.

`activity` - The Activity contains webview. Pass the activity if you want to auto finish
the Activity with current external loading url as result data.