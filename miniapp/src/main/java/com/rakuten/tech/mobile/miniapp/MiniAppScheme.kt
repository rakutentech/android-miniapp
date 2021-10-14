package com.rakuten.tech.mobile.miniapp

import android.content.Context
import android.content.Intent
import android.net.MailTo
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri

internal class MiniAppScheme private constructor(miniAppId: String) {

    val miniAppDomain = "mscheme.$miniAppId"
    val miniAppCustomScheme = "$miniAppDomain://"
    val miniAppCustomDomain = "https://$miniAppDomain/"
    var appUrl: String? = null
        private set

    companion object {
        fun schemeWithAppId(miniAppId: String) = MiniAppScheme(miniAppId)

        fun schemeWithCustomUrl(appUrl: String): MiniAppScheme {
            val scheme = MiniAppScheme("")
            scheme.appUrl = appUrl
            return scheme
        }
    }

    fun isMiniAppUrl(url: String): Boolean = if (appUrl?.isNotEmpty() == true) {
        val miniAppUri = appUrl!!.toUri()
        if (miniAppUri.host?.isNotEmpty() == true) {
            miniAppUri.host.equals(url.toUri().host)
        } else {
            false
        }
    } else {
        url.startsWith(miniAppCustomDomain) || url.startsWith(miniAppCustomScheme)
    }

    fun isDynamicDeepLink(deepLink: String, hostDeepLinks: List<String>) = hostDeepLinks.contains(deepLink)

    fun appendParametersToUrl(url: String, queryParams: String): String {
        return if (queryParams.isEmpty()) url
        else "$url${resolveParameters(queryParams)}"
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun resolveParameters(queryParams: String): String = "?$queryParams"

    internal fun openPhoneDialer(context: Context, url: String) = Intent(Intent.ACTION_DIAL).let {
        it.data = url.toUri()
        startExportedActivity(it, context)
    }

    internal fun openMailComposer(context: Context, url: String) = Intent(Intent.ACTION_SEND).let {
        val mail = MailTo.parse(url)
        it.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail.to))
        it.putExtra(Intent.EXTRA_TEXT, mail.body)
        it.putExtra(Intent.EXTRA_SUBJECT, mail.subject)
        it.putExtra(Intent.EXTRA_CC, arrayOf(mail.cc))
        it.putExtra(Intent.EXTRA_BCC, arrayOf(mail.headers["bcc"]))
        it.type = "message/rfc822"
        startExportedActivity(it, context)
    }

    @VisibleForTesting
    internal fun startExportedActivity(intent: Intent, context: Context): Boolean {
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            true
        } else
            false
    }
}
