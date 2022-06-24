//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.file](../index.md)/[MiniAppFileDownloader](index.md)

# MiniAppFileDownloader

[androidJvm]\
interface [MiniAppFileDownloader](index.md)

The file downloader of a miniapp with onStartFileDownload function. To start file download on the device.

## Functions

| Name | Summary |
|---|---|
| [onStartFileDownload](on-start-file-download.md) | [androidJvm]<br>abstract fun [onStartFileDownload](on-start-file-download.md)(fileName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), url: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), headers: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;, onDownloadSuccess: ([String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), onDownloadFailed: ([MiniAppDownloadFileError](../../com.rakuten.tech.mobile.miniapp.errors/-mini-app-download-file-error/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>For downloading the files which has been invoked by miniapp. |

## Inheritors

| Name |
|---|
| [MiniAppFileDownloaderDefault](../-mini-app-file-downloader-default/index.md) |
