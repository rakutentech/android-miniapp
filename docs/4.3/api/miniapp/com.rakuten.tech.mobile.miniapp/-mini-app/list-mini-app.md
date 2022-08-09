//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniApp](index.md)/[listMiniApp](list-mini-app.md)

# listMiniApp

[androidJvm]\
abstract suspend fun [listMiniApp](list-mini-app.md)(): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[MiniAppInfo](../-mini-app-info/index.md)&gt;

Fetches and lists out the mini applications available in the MiniApp Ecosystem.

#### Return

[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html) of type [MiniAppInfo](../-mini-app-info/index.md) when obtained successfully

## Throws

| | |
|---|---|
| [com.rakuten.tech.mobile.miniapp.MiniAppSdkException](../-mini-app-sdk-exception/index.md) | when fetching fails from the BE server for any reason. |
