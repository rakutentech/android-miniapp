package com.rakuten.tech.mobile.miniapp

internal class MiniAppScheme(miniAppId: String) {

    val miniAppDomain = "mscheme.$miniAppId"
    val miniAppCustomScheme = "$miniAppDomain://"
    val miniAppCustomDomain = "https://$miniAppDomain/"

    fun isMiniAppUrl(url: String) = url.startsWith(miniAppCustomDomain) || url.startsWith(miniAppCustomScheme)

    internal inline fun <T, R> ifLoadingCustomScheme(url: T, callback: (String) -> R): R? =
        if (url is String && url.startsWith(miniAppCustomScheme))
            url.replace(miniAppCustomScheme, miniAppCustomDomain).let(callback)
        else
            null
}
