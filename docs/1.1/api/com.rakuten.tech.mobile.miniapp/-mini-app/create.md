[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](index.md) / [create](./create.md)

# create

`abstract suspend fun create(info: `[`MiniAppInfo`](../-mini-app-info/index.md)`, miniAppMessageBridge: `[`MiniAppMessageBridge`](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md)`): `[`MiniAppDisplay`](../-mini-app-display/index.md)

Creates a mini app.

### Parameters

`info` - metadata of a mini app.
The mini app is downloaded, saved and provides a [MiniAppDisplay](../-mini-app-display/index.md) when successful

`miniAppMessageBridge` - the inferface for exchanging data between mobile hostapp &amp; miniapp

### Exceptions

`MiniAppSdkException` - when there is some issue during fetching,
downloading or creating the view.`abstract suspend fun ~~create~~(info: `[`MiniAppInfo`](../-mini-app-info/index.md)`): `[`MiniAppDisplay`](../-mini-app-display/index.md)
**Deprecated:** Please replace with create(MiniAppInfo, MiniAppMessageBridge)

### Parameters

`info` - metadata of a mini app.
The mini app is downloaded, saved and provides a [MiniAppDisplay](../-mini-app-display/index.md) when successful

### Exceptions

`MiniAppSdkException` - when there is some issue during fetching,
downloading or creating the view.

**Deprecated**
use {@link #create(MiniAppInfo, MiniAppMessageBridge)} instead.
Creates a mini app.

