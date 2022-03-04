//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.navigator](../index.md)/[MiniAppDownloadNavigator](index.md)

# MiniAppDownloadNavigator

[androidJvm]\
interface [MiniAppDownloadNavigator](index.md) : [MiniAppNavigator](../-mini-app-navigator/index.md)

File download controller for mini app view. This interface can optionally be used with your MiniAppNavigator if you wish to intercept file download requests from the mini app. If you do not use this interface, then default handling will be used for file download requests.

## Functions

| Name | Summary |
|---|---|
| [onFileDownloadStart](on-file-download-start.md) | [androidJvm]<br>abstract fun [onFileDownloadStart](on-file-download-start.md)(url: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), userAgent: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), contentDisposition: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), mimetype: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), contentLength: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html))<br>Notify the host application that a file should be downloaded. |
| [openExternalUrl](../-mini-app-navigator/open-external-url.md) | [androidJvm]<br>abstract fun [openExternalUrl](../-mini-app-navigator/open-external-url.md)(url: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), externalResultHandler: [ExternalResultHandler](../-external-result-handler/index.md))<br>Open the external url by browser or webview. |
