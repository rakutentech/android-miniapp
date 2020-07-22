[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](index.md) / [listMiniApp](./list-mini-app.md)

# listMiniApp

`abstract suspend fun listMiniApp(): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`MiniAppInfo`](../-mini-app-info/index.md)`>`

Fetches and lists out the mini applications available in the MiniApp Ecosystem.

### Exceptions

`MiniAppSdkException` - when fetching fails from the BE server for any reason.

**Return**
[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html) of type [MiniAppInfo](../-mini-app-info/index.md) when obtained successfully

