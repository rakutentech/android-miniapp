//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniApp](index.md)/[getMiniAppInfoByPreviewCode](get-mini-app-info-by-preview-code.md)

# getMiniAppInfoByPreviewCode

[androidJvm]\
abstract suspend fun [getMiniAppInfoByPreviewCode](get-mini-app-info-by-preview-code.md)(previewCode: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [PreviewMiniAppInfo](../-preview-mini-app-info/index.md)

Fetches MiniappInfo by preview code.

#### Return

of type [MiniAppInfo](../-mini-app-info/index.md) when obtained successfully

## Throws

| | |
|---|---|
| [com.rakuten.tech.mobile.miniapp.MiniAppSdkException](../-mini-app-sdk-exception/index.md) | when fetching fails from the BE server for any reason. |
