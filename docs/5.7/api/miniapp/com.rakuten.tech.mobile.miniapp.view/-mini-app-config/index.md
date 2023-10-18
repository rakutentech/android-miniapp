//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.view](../index.md)/[MiniAppConfig](index.md)

# MiniAppConfig

[androidJvm]\
data class [MiniAppConfig](index.md)(miniAppSdkConfig: [MiniAppSdkConfig](../../com.rakuten.tech.mobile.miniapp/-mini-app-sdk-config/index.md), miniAppMessageBridge: [MiniAppMessageBridge](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md), miniAppNavigator: [MiniAppNavigator](../../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)?, miniAppFileChooser: [MiniAppFileChooser](../../com.rakuten.tech.mobile.miniapp.file/-mini-app-file-chooser/index.md)?, queryParams: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))

This represents the configuration settings for the Mini App.

## Parameters

androidJvm

| | |
|---|---|
| miniAppSdkConfig | configuration for Mini App SDK. |
| miniAppMessageBridge | the interface for communicating between host app & mini app. |
| miniAppNavigator | allow host app to handle specific urls such as external link. |
| miniAppFileChooser | allow host app to get the file path while choosing file inside the webview. |
| queryParams | the parameters will be appended with the miniapp url scheme. |

## Constructors

| | |
|---|---|
| [MiniAppConfig](-mini-app-config.md) | [androidJvm]<br>fun [MiniAppConfig](-mini-app-config.md)(miniAppSdkConfig: [MiniAppSdkConfig](../../com.rakuten.tech.mobile.miniapp/-mini-app-sdk-config/index.md), miniAppMessageBridge: [MiniAppMessageBridge](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md), miniAppNavigator: [MiniAppNavigator](../../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)?, miniAppFileChooser: [MiniAppFileChooser](../../com.rakuten.tech.mobile.miniapp.file/-mini-app-file-chooser/index.md)?, queryParams: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = "") |

## Properties

| Name | Summary |
|---|---|
| [miniAppFileChooser](mini-app-file-chooser.md) | [androidJvm]<br>val [miniAppFileChooser](mini-app-file-chooser.md): [MiniAppFileChooser](../../com.rakuten.tech.mobile.miniapp.file/-mini-app-file-chooser/index.md)? |
| [miniAppMessageBridge](mini-app-message-bridge.md) | [androidJvm]<br>val [miniAppMessageBridge](mini-app-message-bridge.md): [MiniAppMessageBridge](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md) |
| [miniAppNavigator](mini-app-navigator.md) | [androidJvm]<br>val [miniAppNavigator](mini-app-navigator.md): [MiniAppNavigator](../../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)? |
| [miniAppSdkConfig](mini-app-sdk-config.md) | [androidJvm]<br>val [miniAppSdkConfig](mini-app-sdk-config.md): [MiniAppSdkConfig](../../com.rakuten.tech.mobile.miniapp/-mini-app-sdk-config/index.md) |
| [queryParams](query-params.md) | [androidJvm]<br>var [queryParams](query-params.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
