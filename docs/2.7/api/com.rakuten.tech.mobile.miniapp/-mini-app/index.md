[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](./index.md)

# MiniApp

`abstract class MiniApp`

This represents the contract between the consuming application and the SDK
by which operations in the mini app ecosystem are exposed.
Should be accessed via [MiniApp.instance](instance.md).

### Functions

| [create](create.md) | Creates a mini app. The mini app is downloaded, saved and provides a [MiniAppDisplay](../-mini-app-display/index.md) when successful.`abstract suspend fun create(appId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, miniAppMessageBridge: `[`MiniAppMessageBridge`](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md)`, miniAppNavigator: `[`MiniAppNavigator`](../../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)`? = null): `[`MiniAppDisplay`](../-mini-app-display/index.md)<br>Creates a mini app using the mini app ID and version specified in [MiniAppInfo](../-mini-app-info/index.md). This should only be used in "Preview Mode". The mini app is downloaded, saved and provides a [MiniAppDisplay](../-mini-app-display/index.md) when successful.`abstract suspend fun create(appInfo: `[`MiniAppInfo`](../-mini-app-info/index.md)`, miniAppMessageBridge: `[`MiniAppMessageBridge`](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md)`, miniAppNavigator: `[`MiniAppNavigator`](../../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)`? = null): `[`MiniAppDisplay`](../-mini-app-display/index.md) |
| [createWithUrl](create-with-url.md) | Creates a mini app using provided url. Mini app is NOT downloaded and cached in local, its content are read directly from the url. This should only be used for previewing a mini app from a local server.`abstract suspend fun createWithUrl(appUrl: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, miniAppMessageBridge: `[`MiniAppMessageBridge`](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md)`, miniAppNavigator: `[`MiniAppNavigator`](../../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)`? = null): `[`MiniAppDisplay`](../-mini-app-display/index.md) |
| [fetchInfo](fetch-info.md) | Fetches meta data information of a mini app.`abstract suspend fun fetchInfo(appId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`MiniAppInfo`](../-mini-app-info/index.md) |
| [getCustomPermissions](get-custom-permissions.md) | Get custom permissions with grant results per MiniApp from this SDK.`abstract fun getCustomPermissions(miniAppId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`MiniAppCustomPermission`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission/index.md) |
| [listDownloadedWithCustomPermissions](list-downloaded-with-custom-permissions.md) | lists out the mini applications available with custom permissions in cache.`abstract fun listDownloadedWithCustomPermissions(): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`MiniAppInfo`](../-mini-app-info/index.md)`, `[`MiniAppCustomPermission`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission/index.md)`>>` |
| [listMiniApp](list-mini-app.md) | Fetches and lists out the mini applications available in the MiniApp Ecosystem.`abstract suspend fun listMiniApp(): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`MiniAppInfo`](../-mini-app-info/index.md)`>` |
| [setCustomPermissions](set-custom-permissions.md) | Store custom permissions with grant results per MiniApp inside this SDK.`abstract fun setCustomPermissions(miniAppCustomPermission: `[`MiniAppCustomPermission`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Companion Object Functions

| [instance](instance.md) | Instance of [MiniApp](./index.md) which uses the default config settings, as defined in AndroidManifest.xml. For usual scenarios the default config suffices. However, should it be required to change the config at runtime for QA purpose or similar, another [MiniAppSdkConfig](../-mini-app-sdk-config/index.md) can be provided for customization.`fun instance(settings: `[`MiniAppSdkConfig`](../-mini-app-sdk-config/index.md)` = defaultConfig): `[`MiniApp`](./index.md) |

