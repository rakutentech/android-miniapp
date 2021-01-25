[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.navigator](../index.md) / [ExternalResultHandler](index.md) / [emitResult](./emit-result.md)

# emitResult

`fun emitResult(url: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Notify the result to mini app view.

### Parameters

`url` - Return the current loading url to mini app view.`fun emitResult(intent: `[`Intent`](https://developer.android.com/reference/android/content/Intent.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Notify the result to mini app view. Use this when go with auto close Activity approach.

### Parameters

`intent` - The result intent from closing Activity.

**See Also**

[MiniAppExternalUrlLoader](../-mini-app-external-url-loader/index.md)

