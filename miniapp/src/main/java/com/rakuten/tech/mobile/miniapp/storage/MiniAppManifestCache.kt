package com.rakuten.tech.mobile.miniapp.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import java.lang.Exception

/**
 * A caching class to read and store the [MiniAppManifest] per MiniApp
 * using [SharedPreferences].
 */
internal class MiniAppManifestCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.manifest.cache", Context.MODE_PRIVATE
    )

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun doesDataExist(miniAppId: String) = prefs.contains(miniAppId)

    /**
     * Reads the grant results from SharedPreferences.
     * @param [miniAppId] the key provided to find the stored manifest per MiniApp
     * @return [MiniAppManifest] an object to contain the manifest per MiniApp
     * if data has been stored in cache, otherwise empty value.
     */
    @SuppressLint("RestrictedApi")
    @SuppressWarnings("NestedBlockDepth")
    fun readMiniAppManifest(miniAppId: String): MiniAppManifest {
        val empty = MiniAppManifest(emptyList(), emptyList(), emptyMap())
        return if (doesDataExist(miniAppId)) {
            try {
                val cachedManifest: MiniAppManifest = Gson().fromJson(
                    prefs.getString(miniAppId, ""),
                    object : TypeToken<MiniAppManifest>() {}.type
                )
                Log.d("AAAAA21111~", ""+miniAppId)
                Log.d("AAAAA21111", ""+cachedManifest.toString())
                cachedManifest
            } catch (e: Exception) {
                // if there is any exception, return the empty value
                Log.d("AAAAA22", ""+empty.toString())
                empty
            }
        } else {
            Log.d("AAAAA23", ""+empty.toString())
            // if value hasn't been found in SharedPreferences, save the empty value
            storeMiniAppManifest(miniAppId, empty)
            empty
        }
    }

    /**
     * Stores [MiniAppManifest] to SharedPreferences.
     * @param [miniAppManifest] an object to contain the manifest values per MiniApp.
     */
    fun storeMiniAppManifest(
        miniAppId: String,
        miniAppManifest: MiniAppManifest
    ) {
        Log.d("AAAAA21111~~", ""+miniAppId)
        val jsonToStore: String = Gson().toJson(miniAppManifest)
        prefs.edit().putString(miniAppId, jsonToStore).apply()
        Log.d("AAAAAMP cache set",""+jsonToStore)
    }
}
