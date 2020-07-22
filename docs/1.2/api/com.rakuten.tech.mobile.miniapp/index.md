[miniapp](../index.md) / [com.rakuten.tech.mobile.miniapp](./index.md)

## Package com.rakuten.tech.mobile.miniapp

### Types

| [MiniApp](-mini-app/index.md) | This represents the contract between the consuming application and the SDK by which operations in the mini app ecosystem are exposed. Should be accessed via [MiniApp.instance](-mini-app/instance.md).`abstract class MiniApp` |
| [MiniAppDisplay](-mini-app-display/index.md) | This represents the contract by which the host app can interact with the display unit of the mini app. This contract complies to Android's [LifecycleObserver](#) contract, and when made to observe the lifecycle, it automatically clears up the view state and any services registered with.`interface MiniAppDisplay : LifecycleObserver` |
| [MiniAppInfo](-mini-app-info/index.md) | This represents a Mini App entity.`data class MiniAppInfo : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |
| [MiniAppSdkConfig](-mini-app-sdk-config/index.md) | This represents the configuration settings for the Mini App SDK.`data class MiniAppSdkConfig` |
| [Version](-version/index.md) | This represents a version entity of a Mini App.`data class Version : `[`Parcelable`](https://developer.android.com/reference/android/os/Parcelable.html) |

### Exceptions

| [MiniAppSdkException](-mini-app-sdk-exception/index.md) | A custom exception class which treats the purpose of providing error information to the consumer app in an unified way.`open class MiniAppSdkException : `[`Exception`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html) |

