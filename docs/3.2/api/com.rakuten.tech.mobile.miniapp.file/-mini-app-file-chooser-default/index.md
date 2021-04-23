[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp.file](../index.md) / [MiniAppFileChooserDefault](./index.md)

# MiniAppFileChooserDefault

`class MiniAppFileChooserDefault : `[`MiniAppFileChooser`](../-mini-app-file-chooser/index.md)

The default file chooser of a miniapp.

### Parameters

`requestCode` - of file choosing using an intent inside sdk, which will also be used
to retrieve the data by [Activity.onActivityResult](https://developer.android.com/reference/android/app/Activity.html#onActivityResult(int, int, android.content.Intent)) in the HostApp.

### Constructors

| [&lt;init&gt;](-init-.md) | The default file chooser of a miniapp.`MiniAppFileChooserDefault(requestCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`)` |

### Properties

| [requestCode](request-code.md) | of file choosing using an intent inside sdk, which will also be used to retrieve the data by [Activity.onActivityResult](https://developer.android.com/reference/android/app/Activity.html#onActivityResult(int, int, android.content.Intent)) in the HostApp.`var requestCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |

### Functions

| [onCancel](on-cancel.md) | Can be used when HostApp wants to cancel the file choosing operation.`fun onCancel(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onReceivedFiles](on-received-files.md) | Receive the files from the HostApp.`fun onReceivedFiles(intent: `[`Intent`](https://developer.android.com/reference/android/content/Intent.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onShowFileChooser](on-show-file-chooser.md) | For choosing the files which has been invoked by [WebChromeClient.onShowFileChooser](https://developer.android.com/reference/android/webkit/WebChromeClient.html#onShowFileChooser(android.webkit.WebView, android.webkit.ValueCallback<android.net.Uri[]>, android.webkit.WebChromeClient.FileChooserParams)) inside the miniapp webview.`fun onShowFileChooser(filePathCallback: `[`ValueCallback`](https://developer.android.com/reference/android/webkit/ValueCallback.html)`<`[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`Uri`](https://developer.android.com/reference/android/net/Uri.html)`>>?, fileChooserParams: `[`FileChooserParams`](https://developer.android.com/reference/android/webkit/WebChromeClient/FileChooserParams.html)`?, context: `[`Context`](https://developer.android.com/reference/android/content/Context.html)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

