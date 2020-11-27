[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.js.userinfo](../index.md) / [UserInfoBridgeDispatcher](./index.md)

# UserInfoBridgeDispatcher

`abstract class UserInfoBridgeDispatcher`

A class to provide the interfaces for getting user info e.g. user-name, profile-photo etc.

### Constructors

| [&lt;init&gt;](-init-.md) | A class to provide the interfaces for getting user info e.g. user-name, profile-photo etc.`UserInfoBridgeDispatcher()` |

### Functions

| [getAccessToken](get-access-token.md) | Get access token from host app.`open fun getAccessToken(miniAppId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, onSuccess: (tokenData: `[`TokenData`](../-token-data/index.md)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, onError: (message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [getProfilePhoto](get-profile-photo.md) | Get profile photo url from host app. You can also throw an [Exception](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html) from this method to pass an error message to the mini app.`open fun getProfilePhoto(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [getUserName](get-user-name.md) | Get user name from host app. You can also throw an [Exception](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html) from this method to pass an error message to the mini app.`open fun getUserName(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

