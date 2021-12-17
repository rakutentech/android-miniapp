[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.navigator](../index.md) / [MiniAppNavigator](./index.md)

# MiniAppNavigator

`interface MiniAppNavigator`

The navigation controller of sdk mini app view.
You can optionally pass an implementation of this when creating a mini app using [MiniApp.create](../../com.rakuten.tech.mobile.miniapp/-mini-app/create.md)

### Functions

| [openExternalUrl](open-external-url.md) | Open the external url by browser or webview.`abstract fun openExternalUrl(url: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, externalResultHandler: `[`ExternalResultHandler`](../-external-result-handler/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Inheritors

| [MiniAppDownloadNavigator](../-mini-app-download-navigator/index.md) | File download controller for mini app view. This interface can optionally be used with your MiniAppNavigator if you wish to intercept file download requests from the mini app. If you do not use this interface, then default handling will be used for file download requests.`interface MiniAppDownloadNavigator : `[`MiniAppNavigator`](./index.md) |

