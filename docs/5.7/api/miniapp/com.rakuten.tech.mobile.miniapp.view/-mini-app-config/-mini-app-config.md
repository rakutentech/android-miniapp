//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.view](../index.md)/[MiniAppConfig](index.md)/[MiniAppConfig](-mini-app-config.md)

# MiniAppConfig

[androidJvm]\
fun [MiniAppConfig](-mini-app-config.md)(miniAppSdkConfig: [MiniAppSdkConfig](../../com.rakuten.tech.mobile.miniapp/-mini-app-sdk-config/index.md), miniAppMessageBridge: [MiniAppMessageBridge](../../com.rakuten.tech.mobile.miniapp.js/-mini-app-message-bridge/index.md), miniAppNavigator: [MiniAppNavigator](../../com.rakuten.tech.mobile.miniapp.navigator/-mini-app-navigator/index.md)?, miniAppFileChooser: [MiniAppFileChooser](../../com.rakuten.tech.mobile.miniapp.file/-mini-app-file-chooser/index.md)?, queryParams: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = "")

## Parameters

androidJvm

| | |
|---|---|
| miniAppSdkConfig | configuration for Mini App SDK. |
| miniAppMessageBridge | the interface for communicating between host app & mini app. |
| miniAppNavigator | allow host app to handle specific urls such as external link. |
| miniAppFileChooser | allow host app to get the file path while choosing file inside the webview. |
| queryParams | the parameters will be appended with the miniapp url scheme. |
