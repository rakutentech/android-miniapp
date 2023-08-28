//[miniapp](../../../index.md)/[com.rakuten.tech.mobile.miniapp.view](../index.md)/[MiniAppParameters](index.md)

# MiniAppParameters

[androidJvm]\
sealed class [MiniAppParameters](index.md)

This class can be used in the HostApp to create the mini app views using mini app id.

## Types

| Name | Summary |
|---|---|
| [DefaultParams](-default-params/index.md) | [androidJvm]<br>data class [DefaultParams](-default-params/index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), config: [MiniAppConfig](../-mini-app-config/index.md), miniAppId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), miniAppVersion: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), fromCache: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) : [MiniAppParameters](index.md)<br>This class can be used in the HostApp to create the mini app views using mini app id. |
| [InfoParams](-info-params/index.md) | [androidJvm]<br>data class [InfoParams](-info-params/index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), config: [MiniAppConfig](../-mini-app-config/index.md), miniAppInfo: [MiniAppInfo](../../com.rakuten.tech.mobile.miniapp/-mini-app-info/index.md), fromCache: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) : [MiniAppParameters](index.md)<br>This class can be used in the HostApp to create the mini app views using mini app id. |
| [UrlParams](-url-params/index.md) | [androidJvm]<br>data class [UrlParams](-url-params/index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), config: [MiniAppConfig](../-mini-app-config/index.md), miniAppUrl: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [MiniAppParameters](index.md)<br>This class can be used in the HostApp to create the mini app views using provided url. |

## Inheritors

| Name |
|---|
| [DefaultParams](-default-params/index.md) |
| [InfoParams](-info-params/index.md) |
| [UrlParams](-url-params/index.md) |
