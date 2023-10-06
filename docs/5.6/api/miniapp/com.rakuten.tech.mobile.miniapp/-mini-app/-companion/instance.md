//[miniapp](../../../../index.md)/[com.rakuten.tech.mobile.miniapp](../../index.md)/[MiniApp](../index.md)/[Companion](index.md)/[instance](instance.md)

# instance

[androidJvm]\

@[JvmStatic](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-static/index.html)

fun [instance](instance.md)(settings: [MiniAppSdkConfig](../../-mini-app-sdk-config/index.md) = defaultConfig, setConfigAsDefault: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true): [MiniApp](../index.md)

Instance of [MiniApp](../index.md) which uses the default config settings, as defined in AndroidManifest.xml. For usual scenarios the default config suffices. However, should it be required to change the config at runtime for QA purpose or similar, another [MiniAppSdkConfig](../../-mini-app-sdk-config/index.md) can be provided for customization. [setConfigAsDefault](instance.md) is to use the config as default.

#### Return

[MiniApp](../index.md) instance
