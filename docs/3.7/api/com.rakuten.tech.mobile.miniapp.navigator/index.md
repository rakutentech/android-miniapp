[miniapp](../index.md) / [com.rakuten.tech.mobile.miniapp.navigator](./index.md)

## Package com.rakuten.tech.mobile.miniapp.navigator

### Types

| [ExternalResultHandler](-external-result-handler/index.md) | The url transmitter from external factors to mini app view.`class ExternalResultHandler` |
| [MiniAppDownloadNavigator](-mini-app-download-navigator/index.md) | File download controller for mini app view. This interface can optionally be used with your MiniAppNavigator if you wish to intercept file download requests from the mini app. If you do not use this interface, then default handling will be used for file download requests.`interface MiniAppDownloadNavigator : `[`MiniAppNavigator`](-mini-app-navigator/index.md) |
| [MiniAppExternalUrlLoader](-mini-app-external-url-loader/index.md) | This support the scenario that external loader redirect to url which is only supported in mini app view, close the external loader and emit that url to mini app view by [ExternalResultHandler.emitResult](-external-result-handler/emit-result.md).`class MiniAppExternalUrlLoader` |
| [MiniAppNavigator](-mini-app-navigator/index.md) | The navigation controller of sdk mini app view. You can optionally pass an implementation of this when creating a mini app using [MiniApp.create](../com.rakuten.tech.mobile.miniapp/-mini-app/create.md)`interface MiniAppNavigator` |

