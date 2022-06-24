//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.navigator](../index.md)/[MiniAppDownloadNavigator](index.md)/[onFileDownloadStart](on-file-download-start.md)

# onFileDownloadStart

[androidJvm]\
abstract fun [onFileDownloadStart](on-file-download-start.md)(url: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), userAgent: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), contentDisposition: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), mimetype: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), contentLength: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html))

Notify the host application that a file should be downloaded.

Note that this will only receive requests When the mini app downloaded a file via JavaScript XHR. In this case the [url](on-file-download-start.md) received here will be a base64 data string which you must decode to bytes.

In the case that a mini app wants to download a file from an external URL such as https://www.example.com/test.zip, the request will be sent to [MiniAppNavigator.openExternalUrl](../-mini-app-navigator/open-external-url.md), so you should handle this case in your custom WebView instance using [WebView.setDownloadListener](https://developer.android.com/reference/kotlin/android/webkit/WebView.html#setdownloadlistener).

## Parameters

androidJvm

| | |
|---|---|
| url | The full url to the content that should be downloaded |
| userAgent | the user agent to be used for the download. |
| contentDisposition | Content-disposition http header, if     present. |
| mimetype | The mimetype of the content reported by the server |
| contentLength | The file size reported by the server |
