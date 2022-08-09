//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.file](../index.md)/[MiniAppFileChooserDefault](index.md)

# MiniAppFileChooserDefault

[androidJvm]\
class [MiniAppFileChooserDefault](index.md)(requestCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), miniAppCameraPermissionDispatcher: [MiniAppCameraPermissionDispatcher](../-mini-app-camera-permission-dispatcher/index.md)?) : [MiniAppFileChooser](../-mini-app-file-chooser/index.md)

The default file chooser of a miniapp.

## Parameters

androidJvm

| | |
|---|---|
| requestCode | of file choosing using an intent inside sdk, which will also be used to retrieve the data by [Activity.onActivityResult](https://developer.android.com/reference/kotlin/android/app/Activity.html#onactivityresult) in the HostApp. |
| miniAppCameraPermissionDispatcher | needs to be implemented if HostApp want to access camera from miniapp and HostApp has camera permission in manifest.xml |

## Constructors

| | |
|---|---|
| [MiniAppFileChooserDefault](-mini-app-file-chooser-default.md) | [androidJvm]<br>fun [MiniAppFileChooserDefault](-mini-app-file-chooser-default.md)(requestCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), miniAppCameraPermissionDispatcher: [MiniAppCameraPermissionDispatcher](../-mini-app-camera-permission-dispatcher/index.md)? = null) |

## Functions

| Name | Summary |
|---|---|
| [onCancel](on-cancel.md) | [androidJvm]<br>fun [onCancel](on-cancel.md)()<br>Can be used when HostApp wants to cancel the file choosing operation. |
| [onReceivedFiles](on-received-files.md) | [androidJvm]<br>fun [onReceivedFiles](on-received-files.md)(intent: [Intent](https://developer.android.com/reference/kotlin/android/content/Intent.html)?)<br>Receive the files from the HostApp. |
| [onShowFileChooser](on-show-file-chooser.md) | [androidJvm]<br>open override fun [onShowFileChooser](on-show-file-chooser.md)(filePathCallback: [ValueCallback](https://developer.android.com/reference/kotlin/android/webkit/ValueCallback.html)&lt;[Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[Uri](https://developer.android.com/reference/kotlin/android/net/Uri.html)&gt;&gt;?, fileChooserParams: [WebChromeClient.FileChooserParams](https://developer.android.com/reference/kotlin/android/webkit/WebChromeClient.FileChooserParams.html)?, context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>For choosing the files which has been invoked by [WebChromeClient.onShowFileChooser](https://developer.android.com/reference/kotlin/android/webkit/WebChromeClient.html#onshowfilechooser) inside the miniapp webview. |

## Properties

| Name | Summary |
|---|---|
| [requestCode](request-code.md) | [androidJvm]<br>var [requestCode](request-code.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
