//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.file](../index.md)/[MiniAppFileChooserDefault](index.md)/[onShowFileChooser](on-show-file-chooser.md)

# onShowFileChooser

[androidJvm]\
open override fun [onShowFileChooser](on-show-file-chooser.md)(filePathCallback: [ValueCallback](https://developer.android.com/reference/kotlin/android/webkit/ValueCallback.html)&lt;[Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[Uri](https://developer.android.com/reference/kotlin/android/net/Uri.html)&gt;&gt;?, fileChooserParams: [WebChromeClient.FileChooserParams](https://developer.android.com/reference/kotlin/android/webkit/WebChromeClient.FileChooserParams.html)?, context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

For choosing the files which has been invoked by [WebChromeClient.onShowFileChooser](https://developer.android.com/reference/kotlin/android/webkit/WebChromeClient.html#onshowfilechooser) inside the miniapp webview.

## Parameters

androidJvm

| | |
|---|---|
| filePathCallback | a callback to provide the array of file-paths to select. |
| fileChooserParams | the parameters can be used to customize the options of file chooser. |
| context | the Activity context can be used to start the intent to choose file. |
