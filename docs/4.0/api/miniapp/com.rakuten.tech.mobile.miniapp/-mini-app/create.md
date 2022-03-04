//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp](../index.md)/[MiniApp](index.md)/[create](create.md)

# create

[androidJvm]\
abstract suspend fun [create](create.md)(appId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), miniAppMessageBridge: [MiniAppMessageBridge](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md), miniAppNavigator: [MiniAppNavigator](../../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)? = null, miniAppFileChooser: [MiniAppFileChooser](../../com.rakuten.tech.mobile.miniapp.file/-mini-app-file-chooser/index.md)? = null, queryParams: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = ""): [MiniAppDisplay](../-mini-app-display/index.md)

Creates a mini app. The mini app is downloaded, saved and provides a [MiniAppDisplay](../-mini-app-display/index.md) when successful.

## Parameters

androidJvm

| | |
|---|---|
| appId | mini app id. |
| miniAppMessageBridge | the interface for communicating between host app & mini app. |
| miniAppNavigator | allow host app to handle specific urls such as external link. |
| miniAppFileChooser | allow host app to get the file path while choosing file inside the webview. |
| queryParams | the parameters will be appended with the miniapp url scheme. |

## Throws

| | |
|---|---|
| [com.rakuten.tech.mobile.miniapp.MiniAppNotFoundException](../-mini-app-not-found-exception/index.md) | when the specified project ID does not have any mini app exist on the server. |
| [com.rakuten.tech.mobile.miniapp.MiniAppHasNoPublishedVersionException](../-mini-app-has-no-published-version-exception/index.md) | when the specified mini app ID exists on the server but has no published versions |
| [com.rakuten.tech.mobile.miniapp.MiniAppSdkException](../-mini-app-sdk-exception/index.md) | when there is any other issue during fetching, downloading or creating the view. |
| [com.rakuten.tech.mobile.miniapp.RequiredPermissionsNotGrantedException](../-required-permissions-not-granted-exception/index.md) | when the required permissions of the manifest are not granted. |

[androidJvm]\
abstract suspend fun [create](create.md)(appInfo: [MiniAppInfo](../-mini-app-info/index.md), miniAppMessageBridge: [MiniAppMessageBridge](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md), miniAppNavigator: [MiniAppNavigator](../../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)? = null, miniAppFileChooser: [MiniAppFileChooser](../../com.rakuten.tech.mobile.miniapp.file/-mini-app-file-chooser/index.md)? = null, queryParams: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = ""): [MiniAppDisplay](../-mini-app-display/index.md)

Creates a mini app using the mini app ID and version specified in [MiniAppInfo](../-mini-app-info/index.md). This should only be used in "Preview Mode". The mini app is downloaded, saved and provides a [MiniAppDisplay](../-mini-app-display/index.md) when successful.

## Parameters

androidJvm

| | |
|---|---|
| appInfo | metadata of a mini app. |
| miniAppMessageBridge | the interface for communicating between host app & mini app. |
| miniAppNavigator | allow host app to handle specific urls such as external link. |
| miniAppFileChooser | allow host app to get the file path while choosing file inside the webview. |
| queryParams | the parameters will be appended with the miniapp url scheme. |

## Throws

| | |
|---|---|
| [com.rakuten.tech.mobile.miniapp.MiniAppNotFoundException](../-mini-app-not-found-exception/index.md) | when the specified project ID does not have any mini app exist on the server. |
| [com.rakuten.tech.mobile.miniapp.MiniAppHasNoPublishedVersionException](../-mini-app-has-no-published-version-exception/index.md) | when the specified mini app ID exists on the server but has no published versions |
| [com.rakuten.tech.mobile.miniapp.MiniAppSdkException](../-mini-app-sdk-exception/index.md) | when there is any other issue during fetching, downloading or creating the view. |
| [com.rakuten.tech.mobile.miniapp.RequiredPermissionsNotGrantedException](../-required-permissions-not-granted-exception/index.md) | when the required permissions of the manifest are not granted. |
