[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.file](../index.md) / [MiniAppFileChooser](./index.md)

# MiniAppFileChooser

`interface MiniAppFileChooser`

The file chooser of a miniapp with `onShowFileChooser` function.

### Functions

| [onShowFileChooser](on-show-file-chooser.md) | For choosing the files which has been invoked by [WebChromeClient.onShowFileChooser](https://developer.android.com/reference/android/webkit/WebChromeClient.html#onShowFileChooser(android.webkit.WebView, android.webkit.ValueCallback<android.net.Uri[]>, android.webkit.WebChromeClient.FileChooserParams)) inside the miniapp webview.`abstract fun onShowFileChooser(filePathCallback: `[`ValueCallback`](https://developer.android.com/reference/android/webkit/ValueCallback.html)`<`[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`Uri`](https://developer.android.com/reference/android/net/Uri.html)`>>?, fileChooserParams: `[`FileChooserParams`](https://developer.android.com/reference/android/webkit/WebChromeClient/FileChooserParams.html)`?, context: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

### Inheritors

| [MiniAppFileChooserDefault](../-mini-app-file-chooser-default/index.md) | The default file chooser of a miniapp.`class MiniAppFileChooserDefault : `[`MiniAppFileChooser`](./index.md) |

