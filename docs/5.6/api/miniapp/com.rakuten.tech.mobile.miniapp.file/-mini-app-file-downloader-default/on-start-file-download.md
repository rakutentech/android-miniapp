//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.file](../index.md)/[MiniAppFileDownloaderDefault](index.md)/[onStartFileDownload](on-start-file-download.md)

# onStartFileDownload

[androidJvm]\
open override fun [onStartFileDownload](on-start-file-download.md)(fileName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), url: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), headers: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;, onDownloadSuccess: ([String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), onDownloadFailed: ([MiniAppDownloadFileError](../../com.rakuten.tech.mobile.miniapp.errors/-mini-app-download-file-error/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

For downloading the files which has been invoked by miniapp.

## Parameters

androidJvm

| | |
|---|---|
| fileName | return the name of the file. |
| url | return the download url of the file. |
| headers | returns the header of the file if there is any. |
| onDownloadSuccess | contains file name send from host app to miniapp. |
| onDownloadFailed | contains custom error message send from host app. |
