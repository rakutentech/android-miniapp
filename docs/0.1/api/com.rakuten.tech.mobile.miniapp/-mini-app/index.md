[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](./index.md)

# MiniApp

`abstract class MiniApp`

This represents the contract between the consuming application and the SDK
by which operations in the mini app ecosystem are exposed.
Should be accessed via [MiniApp.instance](instance.md).

### Functions

| [create](create.md) | Creates a mini app from the metadata [MiniAppInfo](../-mini-app-info/index.md) object. The mini app is downloaded, saved and provides a [MiniAppView](../-mini-app-view/index.md) when successful`abstract suspend fun create(appId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, versionId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`MiniAppView`](../-mini-app-view/index.md) |
| [listMiniApp](list-mini-app.md) | Fetches and lists out the mini applications available in the MiniApp Ecosystem.`abstract suspend fun listMiniApp(): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`MiniAppInfo`](../-mini-app-info/index.md)`>` |

### Companion Object Functions

| [instance](instance.md) | Instance of [MiniApp](./index.md).`fun instance(): `[`MiniApp`](./index.md) |

