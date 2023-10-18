//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.file](../index.md)/[MiniAppFileChooser](index.md)

# MiniAppFileChooser

[androidJvm]\
interface [MiniAppFileChooser](index.md)

The file chooser of a miniapp with onShowFileChooser function.

## Functions

| Name | Summary |
|---|---|
| [onShowFileChooser](on-show-file-chooser.md) | [androidJvm]<br>abstract fun [onShowFileChooser](on-show-file-chooser.md)(filePathCallback: [ValueCallback](https://developer.android.com/reference/kotlin/android/webkit/ValueCallback.html)&lt;[Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[Uri](https://developer.android.com/reference/kotlin/android/net/Uri.html)&gt;&gt;?, fileChooserParams: [WebChromeClient.FileChooserParams](https://developer.android.com/reference/kotlin/android/webkit/WebChromeClient.FileChooserParams.html)?, context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>For choosing the files which has been invoked by [WebChromeClient.onShowFileChooser](https://developer.android.com/reference/kotlin/android/webkit/WebChromeClient.html#onshowfilechooser) inside the miniapp webview. |

## Inheritors

| Name |
|---|
| [MiniAppFileChooserDefault](../-mini-app-file-chooser-default/index.md) |
