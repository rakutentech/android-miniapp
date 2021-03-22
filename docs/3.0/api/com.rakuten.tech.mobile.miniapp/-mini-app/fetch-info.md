[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](index.md) / [fetchInfo](./fetch-info.md)

# fetchInfo

`abstract suspend fun fetchInfo(appId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`MiniAppInfo`](../-mini-app-info/index.md)

Fetches meta data information of a mini app.

### Exceptions

`MiniAppNotFoundException` - when the specified project ID does not have any mini app exist on the server.

`MiniAppHasNoPublishedVersionException` - when the specified mini app ID exists on the
server but has no published versions

`MiniAppSdkException` - when fetching fails from the BE server for any other reason.

**Return**
[MiniAppInfo](../-mini-app-info/index.md) for the provided appId of a mini app

