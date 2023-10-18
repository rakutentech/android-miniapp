//[miniapp](../../index.md)/[com.rakuten.tech.mobile.miniapp.navigator](index.md)

# Package com.rakuten.tech.mobile.miniapp.navigator

## Types

| Name | Summary |
|---|---|
| [ExternalResultHandler](-external-result-handler/index.md) | [androidJvm]<br>class [ExternalResultHandler](-external-result-handler/index.md)<br>The url transmitter from external factors to mini app view. |
| [MiniAppDownloadNavigator](-mini-app-download-navigator/index.md) | [androidJvm]<br>interface [MiniAppDownloadNavigator](-mini-app-download-navigator/index.md) : [MiniAppNavigator](-mini-app-navigator/index.md)<br>File download controller for mini app view. This interface can optionally be used with your MiniAppNavigator if you wish to intercept file download requests from the mini app. If you do not use this interface, then default handling will be used for file download requests. |
| [MiniAppExternalUrlLoader](-mini-app-external-url-loader/index.md) | [androidJvm]<br>class [MiniAppExternalUrlLoader](-mini-app-external-url-loader/index.md)<br>This support the scenario that external loader redirect to url which is only supported in mini app view, close the external loader and emit that url to mini app view by [ExternalResultHandler.emitResult](-external-result-handler/emit-result.md). |
| [MiniAppNavigator](-mini-app-navigator/index.md) | [androidJvm]<br>interface [MiniAppNavigator](-mini-app-navigator/index.md)<br>The navigation controller of sdk mini app view. You can optionally pass an implementation of this when creating a mini app using [MiniApp.create](../com.rakuten.tech.mobile.miniapp/-mini-app/create.md) |
