//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniApp](index.md)/[downloadMiniApp](download-mini-app.md)

# downloadMiniApp

[androidJvm]\
abstract suspend fun [downloadMiniApp](download-mini-app.md)(miniAppId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), versionId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), completionHandler: (success: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), [MiniAppSdkException](../-mini-app-sdk-exception/index.md)?) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

download and store a MiniApp.

## Parameters

androidJvm

| | |
|---|---|
| miniAppId | will be the id of the MiniApp. |
| versionId | will be the version of the MiniApp. |
| completionHandler | callback when the MiniApp is successfully downloaded. |
