package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import java.lang.Exception

/**
 * A caching class to read and store the [MiniAppManifest] per MiniApp
 * using [SharedPreferences].
 */
// TODO: unit testcases
internal class MiniAppManifestCache(
    context: Context,
    val miniAppCustomPermissionCache: MiniAppCustomPermissionCache
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.manifest.cache", Context.MODE_PRIVATE
    )

    /**
     * Reads the grant results from SharedPreferences.
     * @param [miniAppId] the key provided to find the stored manifest per MiniApp.
     * @return [MiniAppManifest] an object to contain the manifest per MiniApp.
     * if data has been stored in cache, otherwise empty value.
     */
    fun readMiniAppManifest(miniAppId: String): MiniAppManifest? {
        val manifestJsonStr = prefs.getString(miniAppId, null) ?: return null
        return try {
            Gson().fromJson(manifestJsonStr, object : TypeToken<MiniAppManifest>() {}.type)
        } catch (e: Exception) {
            Log.e(this::class.java.canonicalName, e.message.toString())
            null
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

    fun getCachedAllPermissions(appId: String) = getCachedRequiredPermissions(appId) +
            getCachedOptionalPermissions(appId)

    fun isRequiredPermissionDenied(appId: String): Boolean {
        getCachedRequiredPermissions(appId).find {
            it.second != MiniAppCustomPermissionResult.ALLOWED
        }?.let { return true }
        return false
    }

    private fun getCachedRequiredPermissions(
        appId: String
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val manifest = readMiniAppManifest(appId)
        val cachedPermissions = miniAppCustomPermissionCache.readPermissions(appId).pairValues
        return manifest?.requiredPermissions?.mapNotNull { (first) ->
            cachedPermissions.find { it.first == first }
        }!!
    }

    private fun getCachedOptionalPermissions(
        appId: String
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val manifest = readMiniAppManifest(appId)
        val cachedPermissions = miniAppCustomPermissionCache.readPermissions(appId).pairValues
        return manifest?.optionalPermissions?.mapNotNull { (first) ->
            cachedPermissions.find { it.first == first }
        }!!
    }
}
