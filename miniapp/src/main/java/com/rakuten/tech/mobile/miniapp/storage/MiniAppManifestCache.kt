package com.rakuten.tech.mobile.miniapp.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import java.lang.Exception

/**
 * A caching class to read and store the [MiniAppManifest] per MiniApp
 * using [SharedPreferences].
 */
// TODO: unit testcases
internal class MiniAppManifestCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.manifest.cache", Context.MODE_PRIVATE
    )

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun doesDataExist(miniAppId: String) = prefs.contains(miniAppId)

    /**
     * Reads the grant results from SharedPreferences.
     * @param [miniAppId] the key provided to find the stored manifest per MiniApp.
     * @return [MiniAppManifest] an object to contain the manifest per MiniApp.
     * if data has been stored in cache, otherwise empty value.
     */
    @SuppressLint("RestrictedApi")
    fun readMiniAppManifest(miniAppId: String): MiniAppManifest {
        val empty = MiniAppManifest(emptyList(), emptyList(), emptyMap())
        return if (doesDataExist(miniAppId)) {
            try {
                val cachedManifest: MiniAppManifest = Gson().fromJson(
                    prefs.getString(miniAppId, ""),
                    object : TypeToken<MiniAppManifest>() {}.type
                )
                cachedManifest
            } catch (e: Exception) {
                // if there is any exception, return the empty value
                empty
            }
        } else {
            // if value hasn't been found in SharedPreferences, save the empty value
            storeMiniAppManifest(miniAppId, empty)
            empty
        }
    }

    /**
     * Stores [MiniAppManifest] to SharedPreferences.
     * @param [miniAppId] the key provided to find the stored manifest per MiniApp.
     * @param [miniAppManifest] an object to contain the manifest values per MiniApp.
     */
    fun storeMiniAppManifest(
        miniAppId: String,
        miniAppManifest: MiniAppManifest
    ) {
        val jsonToStore: String = Gson().toJson(miniAppManifest)
        prefs.edit().putString(miniAppId, jsonToStore).apply()
    }
}
