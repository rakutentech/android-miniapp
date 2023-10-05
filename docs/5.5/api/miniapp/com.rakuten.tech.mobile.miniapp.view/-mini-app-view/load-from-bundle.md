//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.view](../index.md)/[MiniAppView](index.md)/[loadFromBundle](load-from-bundle.md)

# loadFromBundle

[androidJvm]\
abstract fun [loadFromBundle](load-from-bundle.md)(manifest: [MiniAppManifest](../../com.rakuten.tech.mobile.miniapp/-mini-app-manifest/index.md)? = null, onComplete: ([MiniAppDisplay](../../com.rakuten.tech.mobile.miniapp/-mini-app-display/index.md)?, [MiniAppSdkException](../../com.rakuten.tech.mobile.miniapp/-mini-app-sdk-exception/index.md)?) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

load a mini app view from bundle. The mini app is downloaded, saved and provides a view when successful.

## Parameters

androidJvm

| | |
|---|---|
| manifest | the parameters will be appended with the miniapp manifest. |
| onComplete | parameters needed to callback when the miniapp is successfully loaded. |
