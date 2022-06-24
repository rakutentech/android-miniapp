//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniApp](index.md)/[createWithUrl](create-with-url.md)

# createWithUrl

[androidJvm]\
abstract suspend fun [createWithUrl](create-with-url.md)(appUrl: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), miniAppMessageBridge: [MiniAppMessageBridge](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md), miniAppNavigator: [MiniAppNavigator](../../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)? = null, miniAppFileChooser: [MiniAppFileChooser](../../com.rakuten.tech.mobile.miniapp.file/-mini-app-file-chooser/index.md)? = null, queryParams: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = ""): [MiniAppDisplay](../-mini-app-display/index.md)

Creates a mini app using provided url. Mini app is NOT downloaded and cached in local, its content are read directly from the url. This should only be used for previewing a mini app from a local server.

## Parameters

androidJvm

| | |
|---|---|
| appUrl | a HTTP url containing Mini App content. |
| miniAppMessageBridge | the interface for communicating between host app & mini app. |
| miniAppNavigator | allow host app to handle specific urls such as external link. |
| miniAppFileChooser | allow host app to get the file path while choosing file inside the webview. |
| queryParams | the parameters will be appended with the miniapp url scheme. |

## Throws

| | |
|---|---|
| [com.rakuten.tech.mobile.miniapp.MiniAppNotFoundException](../-mini-app-not-found-exception/index.md) | when the specified Mini App URL cannot be reached. |
| [com.rakuten.tech.mobile.miniapp.MiniAppSdkException](../-mini-app-sdk-exception/index.md) | when there is any other issue during loading or creating the view. |
