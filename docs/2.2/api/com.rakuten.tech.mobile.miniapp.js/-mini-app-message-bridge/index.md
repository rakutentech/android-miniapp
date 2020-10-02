[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.js](../index.md) / [MiniAppMessageBridge](./index.md)

# MiniAppMessageBridge

`abstract class MiniAppMessageBridge`

Bridge interface for communicating with mini app.

### Constructors

| [&lt;init&gt;](-init-.md) | Bridge interface for communicating with mini app.`MiniAppMessageBridge()` |

### Functions

| [getUniqueId](get-unique-id.md) | Get provided id of mini app for any purpose.`abstract fun getUniqueId(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [postMessage](post-message.md) | Handle the message from external.`fun postMessage(jsonStr: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [requestCustomPermissions](request-custom-permissions.md) | Post custom permissions request.`open fun requestCustomPermissions(permissionsWithDescription: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`MiniAppCustomPermissionType`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-type/index.md)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>>, callback: (`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`MiniAppCustomPermissionType`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-type/index.md)`, `[`MiniAppCustomPermissionResult`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-result/index.md)`>>) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [requestPermission](request-permission.md) | Post permission request from external.`abstract fun requestPermission(miniAppPermissionType: `[`MiniAppPermissionType`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-permission-type/index.md)`, callback: (isGranted: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [shareContent](share-content.md) | Share content info [ShareInfo](#). This info is provided by mini app.`open fun shareContent(content: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, callback: (isSuccess: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

