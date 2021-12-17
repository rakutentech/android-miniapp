[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](index.md) / [getMiniAppInfoByPreviewCode](./get-mini-app-info-by-preview-code.md)

# getMiniAppInfoByPreviewCode

`abstract suspend fun getMiniAppInfoByPreviewCode(previewCode: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`PreviewMiniAppInfo`](../-preview-mini-app-info/index.md)

Fetches MiniappInfo by preview code.

### Exceptions

`MiniAppSdkException` - when fetching fails from the BE server for any reason.

**Return**
of type [MiniAppInfo](../-mini-app-info/index.md) when obtained successfully

