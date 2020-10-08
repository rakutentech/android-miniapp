package com.rakuten.tech.mobile.miniapp

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

internal class MiniAppScheme(miniAppId: String) {

    val miniAppDomain = "mscheme.$miniAppId"
    val miniAppCustomScheme = "$miniAppDomain://"
    val miniAppCustomDomain = "https://$miniAppDomain/"

    fun isMiniAppUrl(url: String) = url.startsWith(miniAppCustomDomain) || url.startsWith(miniAppCustomScheme)

    internal fun openPhoneDialer(context: Context, url: String) = Intent(Intent.ACTION_DIAL).let {
        it.data = url.toUri()
        context.startActivity(it)
    }
}
