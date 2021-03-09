package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import kotlin.Exception

/**
 * A caching class to read and store the [MiniAppManifest] per MiniApp's id & version id using [SharedPreferences].
 */
@Suppress("TooGenericExceptionCaught")
internal class ManifestApiCache(
    context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.manifest.cache.api", Context.MODE_PRIVATE
    )

    fun readManifest(miniAppId: String, versionId: String): MiniAppManifest? {
        val manifestJsonStr = prefs.getString(primaryKey(miniAppId, versionId), null) ?: return null
        return try {
            Gson().fromJson(manifestJsonStr, object : TypeToken<MiniAppManifest>() {}.type)
        } catch (e: Exception) {
            Log.e(this::class.java.canonicalName, e.message.toString())
            null
        }
    }

    fun storeManifest(
        miniAppId: String,
        versionId: String,
        miniAppManifest: MiniAppManifest
    ) {
        val jsonToStore: String = Gson().toJson(miniAppManifest)
        prefs.edit().clear().putString(primaryKey(miniAppId, versionId), jsonToStore).apply()
    }

    private fun primaryKey(miniAppId: String, versionId: String) = "$miniAppId-$versionId"
}
