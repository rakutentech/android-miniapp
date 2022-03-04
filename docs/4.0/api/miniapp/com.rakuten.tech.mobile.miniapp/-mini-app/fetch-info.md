//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniApp](index.md)/[fetchInfo](fetch-info.md)

# fetchInfo

[androidJvm]\
abstract suspend fun [fetchInfo](fetch-info.md)(appId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [MiniAppInfo](../-mini-app-info/index.md)

Fetches meta data information of a mini app.

#### Return

[MiniAppInfo](../-mini-app-info/index.md) for the provided appId of a mini app

## Throws

| | |
|---|---|
| [com.rakuten.tech.mobile.miniapp.MiniAppNotFoundException](../-mini-app-not-found-exception/index.md) | when the specified project ID does not have any mini app exist on the server. |
| [com.rakuten.tech.mobile.miniapp.MiniAppHasNoPublishedVersionException](../-mini-app-has-no-published-version-exception/index.md) | when the specified mini app ID exists on the server but has no published versions |
| [com.rakuten.tech.mobile.miniapp.MiniAppSdkException](../-mini-app-sdk-exception/index.md) | when fetching fails from the BE server for any other reason. |
