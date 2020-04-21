[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](index.md) / [create](./create.md)

# create

`abstract suspend fun create(info: `[`MiniAppInfo`](../-mini-app-info/index.md)`): `[`MiniAppDisplay`](../-mini-app-display/index.md)

Creates a mini app.

### Parameters

`info` - metadata of a mini app.
The mini app is downloaded, saved and provides a [MiniAppDisplay](../-mini-app-display/index.md) when successful

### Exceptions

`MiniAppSdkException` - when there is some issue during fetching,
downloading or creating the view.