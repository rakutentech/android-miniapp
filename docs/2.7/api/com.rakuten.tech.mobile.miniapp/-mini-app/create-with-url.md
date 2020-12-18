[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](index.md) / [createWithUrl](./create-with-url.md)

# createWithUrl

`abstract suspend fun createWithUrl(appUrl: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, miniAppMessageBridge: `[`MiniAppMessageBridge`](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md)`, miniAppNavigator: `[`MiniAppNavigator`](../../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)`? = null): `[`MiniAppDisplay`](../-mini-app-display/index.md)

Creates a mini app using provided url.
Mini app is NOT downloaded and cached in local, its content are read directly from the url.
This should only be used for previewing a mini app from a local server.

### Parameters

`appUrl` - a HTTP url containing Mini App content.

`miniAppMessageBridge` - the interface for communicating between host app &amp; mini app.

`miniAppNavigator` - allow host app to handle specific urls such as external link.

### Exceptions

`MiniAppNotFoundException` - when the specified Mini App URL cannot be reached.

`MiniAppSdkException` - when there is any other issue during loading or creating the view.