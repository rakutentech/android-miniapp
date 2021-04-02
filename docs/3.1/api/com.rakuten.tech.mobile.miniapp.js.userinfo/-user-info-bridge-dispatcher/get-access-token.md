[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.js.userinfo](../index.md) / [UserInfoBridgeDispatcher](index.md) / [getAccessToken](./get-access-token.md)

# getAccessToken

`open fun ~~getAccessToken~~(miniAppId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, onSuccess: (tokenData: `[`TokenData`](../-token-data/index.md)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onError: (message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)
**Deprecated:** This function has been deprecated.

Get access token from host app.

`open fun getAccessToken(miniAppId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, accessTokenScope: `[`AccessTokenScope`](../../com.rakuten.tech.mobile.miniapp.permission/-access-token-scope/index.md)`, onSuccess: (tokenData: `[`TokenData`](../-token-data/index.md)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onError: (message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Get access token from host app.

### Parameters

`accessTokenScope` - contains audience and scope for permission validation.