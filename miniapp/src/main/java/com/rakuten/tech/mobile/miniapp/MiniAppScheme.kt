package com.rakuten.tech.mobile.miniapp

internal class MiniAppScheme(miniAppId: String) {

    val miniAppDomain = "mscheme.$miniAppId"
    val miniAppCustomScheme = "$miniAppDomain://"
    val miniAppCustomDomain = "https://$miniAppDomain/"

    fun isMiniAppUrl(url: String) = url.startsWith(miniAppCustomDomain) || url.startsWith(miniAppCustomScheme)
}
