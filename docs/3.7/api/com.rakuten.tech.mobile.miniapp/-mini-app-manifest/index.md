[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniAppManifest](./index.md)

# MiniAppManifest

`data class MiniAppManifest`

A data class to represent data in the mini app's manifest.

### Constructors

| [&lt;init&gt;](-init-.md) | A data class to represent data in the mini app's manifest.`MiniAppManifest(requiredPermissions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`MiniAppCustomPermissionType`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-type/index.md)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>>, optionalPermissions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`MiniAppCustomPermissionType`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-type/index.md)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>>, accessTokenPermissions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`AccessTokenScope`](../../com.rakuten.tech.mobile.miniapp.permission/-access-token-scope/index.md)`>, customMetaData: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, versionId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)` |

### Properties

| [accessTokenPermissions](access-token-permissions.md) | List of audiences and scopes requested by Mini App.`val accessTokenPermissions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`AccessTokenScope`](../../com.rakuten.tech.mobile.miniapp.permission/-access-token-scope/index.md)`>` |
| [customMetaData](custom-meta-data.md) | Custom metadata set by Mini App.`val customMetaData: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>` |
| [optionalPermissions](optional-permissions.md) | List of optional permissions requested by Mini App.`val optionalPermissions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`MiniAppCustomPermissionType`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-type/index.md)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>>` |
| [requiredPermissions](required-permissions.md) | List of required permissions requested by Mini App.`val requiredPermissions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`MiniAppCustomPermissionType`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-type/index.md)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>>` |
| [versionId](version-id.md) | the version id for the Mini App.`val versionId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

