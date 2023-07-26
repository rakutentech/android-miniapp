//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.file](../index.md)/[MiniAppFileDownloaderDefault](index.md)

# MiniAppFileDownloaderDefault

[androidJvm]\
class [MiniAppFileDownloaderDefault](index.md)(activity: [Activity](https://developer.android.com/reference/kotlin/android/app/Activity.html), requestCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [MiniAppFileDownloader](../-mini-app-file-downloader/index.md)

The default file downloader of a miniapp.

## Parameters

androidJvm

| | |
|---|---|
| requestCode | of file downloading using an intent inside sdk, which will also be used to retrieve the Uri of the file by [Activity.onActivityResult](https://developer.android.com/reference/kotlin/android/app/Activity.html#onactivityresult) in the HostApp. |

## Constructors

| | |
|---|---|
| [MiniAppFileDownloaderDefault](-mini-app-file-downloader-default.md) | [androidJvm]<br>fun [MiniAppFileDownloaderDefault](-mini-app-file-downloader-default.md)(activity: [Activity](https://developer.android.com/reference/kotlin/android/app/Activity.html), requestCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Functions

| Name | Summary |
|---|---|
| [onCancel](on-cancel.md) | [androidJvm]<br>fun [onCancel](on-cancel.md)()<br>Can be used when HostApp wants to cancel the file download operation. |
| [onReceivedResult](on-received-result.md) | [androidJvm]<br>fun [onReceivedResult](on-received-result.md)(destinationUri: [Uri](https://developer.android.com/reference/kotlin/android/net/Uri.html))<br>Retrieve the Uri of the file by [Activity.onActivityResult](https://developer.android.com/reference/kotlin/android/app/Activity.html#onactivityresult) in the HostApp. |
| [onStartFileDownload](on-start-file-download.md) | [androidJvm]<br>open override fun [onStartFileDownload](on-start-file-download.md)(fileName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), url: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), headers: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;, onDownloadSuccess: ([String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), onDownloadFailed: ([MiniAppDownloadFileError](../../com.rakuten.tech.mobile.miniapp.errors/-mini-app-download-file-error/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))<br>For downloading the files which has been invoked by miniapp. |

## Properties

| Name | Summary |
|---|---|
| [activity](activity.md) | [androidJvm]<br>var [activity](activity.md): [Activity](https://developer.android.com/reference/kotlin/android/app/Activity.html) |
| [requestCode](request-code.md) | [androidJvm]<br>var [requestCode](request-code.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
