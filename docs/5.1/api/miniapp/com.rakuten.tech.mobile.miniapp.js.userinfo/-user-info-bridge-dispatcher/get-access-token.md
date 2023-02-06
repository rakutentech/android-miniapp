//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.js.userinfo](../index.md)/[UserInfoBridgeDispatcher](index.md)/[getAccessToken](get-access-token.md)

# getAccessToken

[androidJvm]\

@[JvmName](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-name/index.html)(name = "getAccessToken")

open fun [getAccessToken](get-access-token.md)(miniAppId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), accessTokenScope: [AccessTokenScope](../../com.rakuten.tech.mobile.miniapp.permission/-access-token-scope/index.md), onSuccess: (tokenData: [TokenData](../-token-data/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), onError: (tokenError: [MiniAppAccessTokenError](../../com.rakuten.tech.mobile.miniapp.errors/-mini-app-access-token-error/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

Get access token from host app.

## Parameters

androidJvm

| | |
|---|---|
| accessTokenScope | contains audience and scope for permission validation. |
| onError | contains custom error message send from host app |
