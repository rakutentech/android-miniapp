[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.api](../index.md) / [MetadataResponse](./index.md)

# MetadataResponse

`data class MetadataResponse`

Metadata response object includes required and optional permissions.

### Constructors

| [&lt;init&gt;](-init-.md) | Metadata response object includes required and optional permissions.`MetadataResponse(requiredPermissions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`MetadataPermissionObj`](../-metadata-permission-obj/index.md)`>?, optionalPermissions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`MetadataPermissionObj`](../-metadata-permission-obj/index.md)`>?, accessTokenPermissions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`AccessTokenScope`](../../com.rakuten.tech.mobile.miniapp.permission/-access-token-scope/index.md)`>?, customMetaData: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>?)` |

### Properties

| [accessTokenPermissions](access-token-permissions.md) | `val accessTokenPermissions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`AccessTokenScope`](../../com.rakuten.tech.mobile.miniapp.permission/-access-token-scope/index.md)`>?` |
| [customMetaData](custom-meta-data.md) | `val customMetaData: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>?` |
| [optionalPermissions](optional-permissions.md) | `val optionalPermissions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`MetadataPermissionObj`](../-metadata-permission-obj/index.md)`>?` |
| [requiredPermissions](required-permissions.md) | `val requiredPermissions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`MetadataPermissionObj`](../-metadata-permission-obj/index.md)`>?` |

