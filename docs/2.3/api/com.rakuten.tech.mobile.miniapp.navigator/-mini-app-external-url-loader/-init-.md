[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.navigator](../index.md) / [MiniAppExternalUrlLoader](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`MiniAppExternalUrlLoader(miniAppId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, activity: `[`Activity`](https://developer.android.com/reference/android/app/Activity.html)`? = null)`

This support the scenario that external loader redirect to url which is only supported in mini app view,
close the external loader and emit that url to mini app view by [ExternalResultHandler.emitResult](../-external-result-handler/emit-result.md).

### Parameters

`miniAppId` - The id of loading mini app.

`activity` - The Activity contains webview. Pass the activity if you want to auto finish
the Activity with current external loading url as result data.