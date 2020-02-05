[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](index.md) / [create](./create.md)

# create

`abstract suspend fun create(appId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, versionId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`MiniAppView`](../-mini-app-view/index.md)

Creates a mini app from the metadata [MiniAppInfo](../-mini-app-info/index.md) object.
The mini app is downloaded, saved and provides a [MiniAppView](../-mini-app-view/index.md) when successful

### Exceptions

`MiniAppSdkException` - when there is some issue during fetching,
downloading or creating the view.