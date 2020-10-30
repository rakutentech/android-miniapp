[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.js](../index.md) / [MiniAppMessageBridge](index.md) / [requestCustomPermissions](./request-custom-permissions.md)

# requestCustomPermissions

`open fun requestCustomPermissions(permissionsWithDescription: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`MiniAppCustomPermissionType`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-type/index.md)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>>, callback: (`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`MiniAppCustomPermissionType`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-type/index.md)`, `[`MiniAppCustomPermissionResult`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission-result/index.md)`>>) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Post custom permissions request.

### Parameters

`permissionsWithDescription` - list of name and descriptions of custom permissions sent from external.

`callback` - to invoke a list of name and grant results of custom permissions sent from hostapp.