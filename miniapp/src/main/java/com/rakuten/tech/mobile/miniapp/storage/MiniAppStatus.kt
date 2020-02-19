package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.content.SharedPreferences

internal class MiniAppStatus(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.storage", Context.MODE_PRIVATE
    )

    fun setVersionDownloaded(appId: String, versionId: String, value: Boolean) =
        prefs.edit().putBoolean(appId + versionId, value).apply()

    fun isVersionDownloaded(appId: String, versionId: String, default: Boolean = false): Boolean =
        prefs.getBoolean(appId + versionId, default)
}
