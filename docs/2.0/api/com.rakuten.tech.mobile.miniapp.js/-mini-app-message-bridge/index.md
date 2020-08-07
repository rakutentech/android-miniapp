[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.js](../index.md) / [MiniAppMessageBridge](./index.md)

# MiniAppMessageBridge

`abstract class MiniAppMessageBridge`

Bridge interface for communicating with mini app.

### Constructors

| [&lt;init&gt;](-init-.md) | Bridge interface for communicating with mini app.`MiniAppMessageBridge()` |

### Functions

| [getUniqueId](get-unique-id.md) | Get provided id of mini app for any purpose.`abstract fun getUniqueId(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [postMessage](post-message.md) | Handle the message from external.`fun postMessage(jsonStr: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [requestPermission](request-permission.md) | Post permission request from external.`abstract fun requestPermission(miniAppPermissionType: `[`MiniAppPermissionType`](../-mini-app-permission-type/index.md)`, callback: (isGranted: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

