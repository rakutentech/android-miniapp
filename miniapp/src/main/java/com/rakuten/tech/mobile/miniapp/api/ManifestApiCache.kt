package com.rakuten.tech.mobile.miniapp.api

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.MiniAppManifest

/**
 * A caching class to read and store the [MiniAppManifest] per miniapp id & version id using [SharedPreferences].
 */
@Suppress("TooGenericExceptionCaught")
internal class ManifestApiCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.manifest.cache.api", Context.MODE_PRIVATE
    )

    /**
     * Reads the api manifest from SharedPreferences.
     * @param [miniAppId] the first part of key provided to store manifest.
     * @param [versionId] the second part of key provided to store manifest.
     * @param [languageCode] the third part of key provided to store manifest.
     * @return [MiniAppManifest] an object to contain the manifest information,
     * if data has been stored in cache, otherwise null.
     */
    fun readManifest(miniAppId: String, versionId: String, languageCode: String): MiniAppManifest? {
        val manifestJsonStr = prefs.getString(primaryKey(miniAppId, versionId, languageCode), null) ?: return null
        return try {
            Gson().fromJson(manifestJsonStr, object : TypeToken<MiniAppManifest>() {}.type)
        } catch (e: Exception) {
            Log.e(this::class.java.canonicalName, e.message.toString())
            null
        }
    }

    /**
     * Stores the api manifest to SharedPreferences.
     * @param [miniAppId] the first part of key provided to store manifest.
     * @param [versionId] the second part of key provided to store manifest.
     * @param [languageCode] the third part of key provided to store manifest.
     * @return [MiniAppManifest] an object to contain the manifest information.
     */
    @SuppressLint("CommitPrefEdits")
    fun storeManifest(miniAppId: String, versionId: String, languageCode: String, miniAppManifest: MiniAppManifest) {
        val jsonToStore: String = Gson().toJson(miniAppManifest)
        prefs.edit().clear().putString(primaryKey(miniAppId, versionId, languageCode), jsonToStore).apply()
    }

    @VisibleForTesting
    fun primaryKey(miniAppId: String, versionId: String, languageCode: String) = "$miniAppId-$versionId-$languageCode"
}
