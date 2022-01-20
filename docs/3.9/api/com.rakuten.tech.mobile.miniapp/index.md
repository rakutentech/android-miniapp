[miniapp](../index.md) / [com.rakuten.tech.mobile.miniapp](./index.md)

## Package com.rakuten.tech.mobile.miniapp

### Types

| [Host](-host/index.md) | This represents a host entity of a Mini App.`data class Host : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [MiniApp](-mini-app/index.md) | This represents the contract between the consuming application and the SDK by which operations in the mini app ecosystem are exposed. Should be accessed via [MiniApp.instance](-mini-app/instance.md).`abstract class MiniApp` |
| [MiniAppDisplay](-mini-app-display/index.md) | This represents the contract by which the host app can interact with the display unit of the mini app. This contract complies to Android's [LifecycleObserver](#) contract, and when made to observe the lifecycle, it automatically clears up the view state and any services registered with.`interface MiniAppDisplay : LifecycleObserver` |
| [MiniAppInfo](-mini-app-info/index.md) | This represents a Mini App entity.`data class MiniAppInfo : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [MiniAppManifest](-mini-app-manifest/index.md) | A data class to represent data in the mini app's manifest.`data class MiniAppManifest` |
| [MiniAppSdkConfig](-mini-app-sdk-config/index.md) | This represents the configuration settings for the Mini App SDK.`data class MiniAppSdkConfig : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [PreviewMiniAppInfo](-preview-mini-app-info/index.md) | This represents a response entity for preview code.`data class PreviewMiniAppInfo : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [Version](-version/index.md) | This represents a version entity of a Mini App.`data class Version : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |

### Exceptions

| [MiniAppHasNoPublishedVersionException](-mini-app-has-no-published-version-exception/index.md) | Exception which is thrown when the server returns no published versions for the provided mini app ID.`class MiniAppHasNoPublishedVersionException : `[`MiniAppSdkException`](-mini-app-sdk-exception/index.md) |
| [MiniAppHostException](-mini-app-host-exception/index.md) | Exception which is thrown when the provided project ID does not have any mini app exist on the server.`class MiniAppHostException : `[`MiniAppSdkException`](-mini-app-sdk-exception/index.md) |
| [MiniAppNetException](-mini-app-net-exception/index.md) | Exception indicating that there was an issue with network connectivity.`class MiniAppNetException : `[`MiniAppSdkException`](-mini-app-sdk-exception/index.md) |
| [MiniAppNotFoundException](-mini-app-not-found-exception/index.md) | Exception which is thrown when the provided project ID does not have any mini app exist on the server.`class MiniAppNotFoundException : `[`MiniAppSdkException`](-mini-app-sdk-exception/index.md) |
| [MiniAppSdkException](-mini-app-sdk-exception/index.md) | A custom exception class which treats the purpose of providing error information to the consumer app in an unified way.`open class MiniAppSdkException : `[`Exception`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html) |
| [MiniAppVerificationException](-mini-app-verification-exception/index.md) | Exception which is thrown when cannot verify device keystore.`class MiniAppVerificationException : `[`MiniAppSdkException`](-mini-app-sdk-exception/index.md) |
| [RequiredPermissionsNotGrantedException](-required-permissions-not-granted-exception/index.md) | Exception which is thrown when the required permissions of the manifest are not granted.`class RequiredPermissionsNotGrantedException : `[`MiniAppSdkException`](-mini-app-sdk-exception/index.md) |
| [SSLCertificatePinnigException](-s-s-l-certificate-pinnig-exception/index.md) | Exception which is thrown when the public key used for ssl pinning is mismatched.`class SSLCertificatePinnigException : `[`MiniAppSdkException`](-mini-app-sdk-exception/index.md) |

