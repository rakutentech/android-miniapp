//[miniapp](../../index.md)/[com.rakuten.tech.mobile.miniapp.file](index.md)

# Package com.rakuten.tech.mobile.miniapp.file

## Types

| Name | Summary |
|---|---|
| [MiniAppCameraPermissionDispatcher](-mini-app-camera-permission-dispatcher/index.md) | [androidJvm]<br>interface [MiniAppCameraPermissionDispatcher](-mini-app-camera-permission-dispatcher/index.md)<br>A class to provide the interfaces for getting and requesting camera permission. |
| [MiniAppFileChooser](-mini-app-file-chooser/index.md) | [androidJvm]<br>interface [MiniAppFileChooser](-mini-app-file-chooser/index.md)<br>The file chooser of a miniapp with onShowFileChooser function. |
| [MiniAppFileChooserDefault](-mini-app-file-chooser-default/index.md) | [androidJvm]<br>class [MiniAppFileChooserDefault](-mini-app-file-chooser-default/index.md)(requestCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), miniAppCameraPermissionDispatcher: [MiniAppCameraPermissionDispatcher](-mini-app-camera-permission-dispatcher/index.md)?) : [MiniAppFileChooser](-mini-app-file-chooser/index.md)<br>The default file chooser of a miniapp. |
| [MiniAppFileDownloader](-mini-app-file-downloader/index.md) | [androidJvm]<br>interface [MiniAppFileDownloader](-mini-app-file-downloader/index.md)<br>The file downloader of a miniapp with onStartFileDownload function. To start file download on the device. |
| [MiniAppFileDownloaderDefault](-mini-app-file-downloader-default/index.md) | [androidJvm]<br>class [MiniAppFileDownloaderDefault](-mini-app-file-downloader-default/index.md)(activity: [Activity](https://developer.android.com/reference/kotlin/android/app/Activity.html), requestCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [MiniAppFileDownloader](-mini-app-file-downloader/index.md)<br>The default file downloader of a miniapp. |
