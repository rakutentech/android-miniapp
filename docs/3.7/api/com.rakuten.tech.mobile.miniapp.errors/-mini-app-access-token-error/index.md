[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.errors](../index.md) / [MiniAppAccessTokenError](./index.md)

# MiniAppAccessTokenError

`class MiniAppAccessTokenError : `[`MiniAppBridgeError`](../-mini-app-bridge-error/index.md)

A class to provide the custom errors specific for access token.

### Constructors

| [&lt;init&gt;](-init-.md) | A class to provide the custom errors specific for access token.`MiniAppAccessTokenError(type: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null)` |

### Properties

| [message](message.md) | `val message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [type](type.md) | `val type: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |

### Companion Object Properties

| [audienceNotSupportedError](audience-not-supported-error.md) | `val audienceNotSupportedError: `[`MiniAppAccessTokenError`](./index.md) |
| [scopesNotSupportedError](scopes-not-supported-error.md) | `val scopesNotSupportedError: `[`MiniAppAccessTokenError`](./index.md) |

### Companion Object Functions

| [authorizationFailureError](authorization-failure-error.md) | send custom error message for authorization fail from host app.`fun authorizationFailureError(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`MiniAppAccessTokenError`](./index.md) |
| [custom](custom.md) | send custom error message from host app.`fun custom(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`MiniAppAccessTokenError`](./index.md) |

