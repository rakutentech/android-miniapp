[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.navigator](../index.md) / [MiniAppDownloadNavigator](./index.md)

# MiniAppDownloadNavigator

`interface MiniAppDownloadNavigator : `[`MiniAppNavigator`](../-mini-app-navigator/index.md)

File download controller for mini app view.
This interface can optionally be used with your MiniAppNavigator if you wish to intercept
file download requests from the mini app. If you do not use this interface,
then default handling will be used for file download requests.

### Functions

| [onFileDownloadStart](on-file-download-start.md) | Notify the host application that a file should be downloaded.`abstract fun onFileDownloadStart(url: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, userAgent: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, contentDisposition: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, mimetype: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, contentLength: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

