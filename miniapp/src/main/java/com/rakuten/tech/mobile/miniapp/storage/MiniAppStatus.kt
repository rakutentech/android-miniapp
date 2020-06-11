package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import java.io.File

private const val LOCAL_VERSION = "local_version"

internal class MiniAppStatus(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.storage", Context.MODE_PRIVATE
    )

    var localVersion: String?
        get() = prefs.getString(LOCAL_VERSION, null)
        set(downloadedVersion) = prefs.edit().putString(LOCAL_VERSION, downloadedVersion).apply()

    fun setVersionDownloaded(appId: String, versionId: String, value: Boolean) =
        prefs.edit().putBoolean(appId + versionId, value).apply()

    fun isVersionDownloaded(
        appId: String,
        versionId: String,
        versionPath: String,
        default: Boolean = false
    ): Boolean =
        isExisted(versionPath) && prefs.getBoolean(appId + versionId, default)

    @VisibleForTesting
    internal fun isExisted(filePath: String): Boolean = File(filePath).exists()
}
