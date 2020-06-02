[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](./index.md)

# MiniApp

`abstract class MiniApp`

This represents the contract between the consuming application and the SDK
by which operations in the mini app ecosystem are exposed.
Should be accessed via [MiniApp.instance](instance.md).

### Functions

| [create](create.md) | Creates a mini app.`abstract suspend fun create(info: `[`MiniAppInfo`](../-mini-app-info/index.md)`, miniAppMessageBridge: `[`MiniAppMessageBridge`](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md)`): `[`MiniAppDisplay`](../-mini-app-display/index.md)`abstract suspend fun ~~create~~(info: `[`MiniAppInfo`](../-mini-app-info/index.md)`): `[`MiniAppDisplay`](../-mini-app-display/index.md) |
| [fetchInfo](fetch-info.md) | Fetches meta data information of a mini app.`abstract suspend fun fetchInfo(appId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`MiniAppInfo`](../-mini-app-info/index.md) |
| [listMiniApp](list-mini-app.md) | Fetches and lists out the mini applications available in the MiniApp Ecosystem.`abstract suspend fun listMiniApp(): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`MiniAppInfo`](../-mini-app-info/index.md)`>` |

### Companion Object Functions

| [instance](instance.md) | Instance of [MiniApp](./index.md) which uses the default config settings, as defined in AndroidManifest.xml. For usual scenarios the default config suffices. However, should it be required to change the config at runtime for QA purpose or similar, another [MiniAppSdkConfig](../-mini-app-sdk-config/index.md) can be provided for customization.`fun instance(settings: `[`MiniAppSdkConfig`](../-mini-app-sdk-config/index.md)` = defaultConfig): `[`MiniApp`](./index.md) |

