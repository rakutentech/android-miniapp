package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import java.io.File

internal class MiniAppStatus(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.storage", Context.MODE_PRIVATE
    )
    private val gson = Gson()

    fun saveDownloadedMiniApp(miniAppInfo: MiniAppInfo) =
        prefs.edit().putString(miniAppInfo.id, gson.toJson(miniAppInfo)).apply()

    @Suppress("SwallowedException")
    fun getDownloadedMiniApp(appId: String): MiniAppInfo? =
        try {
            gson.fromJson(prefs.getString(appId, ""), MiniAppInfo::class.java)
        } catch (error: JsonSyntaxException) {
            null
        }

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
