[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](index.md) / [create](./create.md)

# create

`abstract suspend fun create(appId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, miniAppMessageBridge: `[`MiniAppMessageBridge`](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md)`): `[`MiniAppDisplay`](../-mini-app-display/index.md)

Creates a mini app.
The mini app is downloaded, saved and provides a [MiniAppDisplay](../-mini-app-display/index.md) when successful.

### Parameters

`appId` - mini app id.

`miniAppMessageBridge` - the interface for communicating between host app &amp; mini app

### Exceptions

`MiniAppSdkException` - when there is some issue during fetching,
downloading or creating the view.`abstract suspend fun create(appId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, miniAppMessageBridge: `[`MiniAppMessageBridge`](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md)`, miniAppNavigator: `[`MiniAppNavigator`](../../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)`): `[`MiniAppDisplay`](../-mini-app-display/index.md)

Same as [createString,MiniAppMessageBridge](#).
Use this to control external url loader.

### Parameters

`miniAppNavigator` - allow host app to handle specific urls such as external link.