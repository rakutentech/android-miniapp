[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniAppSdkConfig](./index.md)

# MiniAppSdkConfig

`data class MiniAppSdkConfig`

This represents the configuration settings for the Mini App SDK.

### Constructors

| [&lt;init&gt;](-init-.md) | `MiniAppSdkConfig(baseUrl: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, rasAppId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, rasProjectId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = rasAppId, subscriptionKey: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, hostAppVersionId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", hostAppUserAgentInfo: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, isTestMode: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, isPreviewMode: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = isTestMode)`<br>This represents the configuration settings for the Mini App SDK.`MiniAppSdkConfig(baseUrl: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, rasProjectId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, subscriptionKey: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, hostAppVersionId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", hostAppUserAgentInfo: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, isPreviewMode: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`)` |

### Properties

| [baseUrl](base-url.md) | Base URL used for retrieving a Mini App.`val baseUrl: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [hostAppUserAgentInfo](host-app-user-agent-info.md) | User Agent information from Host App.`val hostAppUserAgentInfo: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [hostAppVersionId](host-app-version-id.md) | Version of the host app, used to determine feature compatibility for Mini App.`val hostAppVersionId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [isPreviewMode](is-preview-mode.md) | Whether the host app wants to use the API Endpoints under "Preview" mode.`val isPreviewMode: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [rasProjectId](ras-project-id.md) | Project ID for the Platform API.`val rasProjectId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [subscriptionKey](subscription-key.md) | Subscription Key for the Platform API.`val subscriptionKey: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

