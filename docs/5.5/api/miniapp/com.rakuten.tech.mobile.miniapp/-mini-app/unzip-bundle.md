//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniApp](index.md)/[unzipBundle](unzip-bundle.md)

# unzipBundle

[androidJvm]\
abstract suspend fun [unzipBundle](unzip-bundle.md)(fileName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), miniAppId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), versionId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), completionHandler: (success: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), [MiniAppSdkException](../-mini-app-sdk-exception/index.md)?) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)? = null)

unzip and store a MiniApp bundle from asset folder of host app.

## Parameters

androidJvm

| | |
|---|---|
| fileName | will be the name of the file in asset folder. |
| miniAppId | will be the id of the MiniApp. |
| versionId | will be the version of the MiniApp. |
| completionHandler | callback when the bundle is successfully unzipped. |
