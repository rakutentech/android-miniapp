[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](index.md) / [getMiniAppManifest](./get-mini-app-manifest.md)

# getMiniAppManifest

`abstract suspend fun getMiniAppManifest(appId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, versionId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, languageCode: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = ""): `[`MiniAppManifest`](../-mini-app-manifest/index.md)

Get the manifest information e.g. required and optional permissions.

### Parameters

`appId` - mini app id.

`versionId` - of mini app.

`languageCode` - of mini app.

**Return**
MiniAppManifest an object contains manifest information of a mini app.

