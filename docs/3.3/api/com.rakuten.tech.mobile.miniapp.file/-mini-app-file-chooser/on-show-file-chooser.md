[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.file](../index.md) / [MiniAppFileChooser](index.md) / [onShowFileChooser](./on-show-file-chooser.md)

# onShowFileChooser

`abstract fun onShowFileChooser(filePathCallback: `[`ValueCallback`](https://developer.android.com/reference/android/webkit/ValueCallback.html)`<`[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`Uri`](https://developer.android.com/reference/android/net/Uri.html)`>>?, fileChooserParams: `[`FileChooserParams`](https://developer.android.com/reference/android/webkit/WebChromeClient/FileChooserParams.html)`?, context: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

For choosing the files which has been invoked by [WebChromeClient.onShowFileChooser](https://developer.android.com/reference/android/webkit/WebChromeClient.html#onShowFileChooser(android.webkit.WebView, android.webkit.ValueCallback<android.net.Uri[]>, android.webkit.WebChromeClient.FileChooserParams))
inside the miniapp webview.

### Parameters

`filePathCallback` - a callback to provide the array of file-paths to select.

`fileChooserParams` - the parameters can be used to customize the options of file chooser.

`context` - the Activity context can be used to start the intent to choose file.