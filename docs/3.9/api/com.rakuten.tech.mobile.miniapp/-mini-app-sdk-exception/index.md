[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniAppSdkException](./index.md)

# MiniAppSdkException

`open class MiniAppSdkException : `[`Exception`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html)

A custom exception class which treats the purpose of providing
error information to the consumer app in an unified way.

### Constructors

| [&lt;init&gt;](-init-.md) | `MiniAppSdkException(e: `[`Exception`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html)`)`<br>`MiniAppSdkException(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)`<br>A custom exception class which treats the purpose of providing error information to the consumer app in an unified way.`MiniAppSdkException(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, cause: `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`?)` |

### Inheritors

| [MiniAppHasNoPublishedVersionException](../-mini-app-has-no-published-version-exception/index.md) | Exception which is thrown when the server returns no published versions for the provided mini app ID.`class MiniAppHasNoPublishedVersionException : `[`MiniAppSdkException`](./index.md) |
| [MiniAppHostException](../-mini-app-host-exception/index.md) | Exception which is thrown when the provided project ID does not have any mini app exist on the server.`class MiniAppHostException : `[`MiniAppSdkException`](./index.md) |
| [MiniAppNetException](../-mini-app-net-exception/index.md) | Exception indicating that there was an issue with network connectivity.`class MiniAppNetException : `[`MiniAppSdkException`](./index.md) |
| [MiniAppNotFoundException](../-mini-app-not-found-exception/index.md) | Exception which is thrown when the provided project ID does not have any mini app exist on the server.`class MiniAppNotFoundException : `[`MiniAppSdkException`](./index.md) |
| [MiniAppVerificationException](../-mini-app-verification-exception/index.md) | Exception which is thrown when cannot verify device keystore.`class MiniAppVerificationException : `[`MiniAppSdkException`](./index.md) |
| [RequiredPermissionsNotGrantedException](../-required-permissions-not-granted-exception/index.md) | Exception which is thrown when the required permissions of the manifest are not granted.`class RequiredPermissionsNotGrantedException : `[`MiniAppSdkException`](./index.md) |
| [SSLCertificatePinnigException](../-s-s-l-certificate-pinnig-exception/index.md) | Exception which is thrown when the public key used for ssl pinning is mismatched.`class SSLCertificatePinnigException : `[`MiniAppSdkException`](./index.md) |

