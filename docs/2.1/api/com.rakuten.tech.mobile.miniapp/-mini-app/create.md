[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](index.md) / [create](./create.md)

# create

`abstract suspend fun create(appId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, miniAppMessageBridge: `[`MiniAppMessageBridge`](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md)`): `[`MiniAppDisplay`](../-mini-app-display/index.md)

Creates a mini app.

### Parameters

`appId` - mini app id.
The mini app is downloaded, saved and provides a [MiniAppDisplay](../-mini-app-display/index.md) when successful

`miniAppMessageBridge` - the interface for communicating between host app &amp; mini app

### Exceptions

`MiniAppSdkException` - when there is some issue during fetching,
downloading or creating the view.