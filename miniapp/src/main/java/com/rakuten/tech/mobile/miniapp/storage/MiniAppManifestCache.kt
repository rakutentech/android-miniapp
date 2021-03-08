package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.MiniAppManifest
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import kotlin.Exception

/**
 * A caching class to read and store the [MiniAppManifest] per MiniApp using [SharedPreferences].
 */
@Suppress("TooGenericExceptionCaught")
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

    /**
     * Returns the list of all manifest permissions e.g. required and optional.
     * @param [miniAppId] the key provided to find the stored manifest per MiniApp.
     */
    fun getCachedAllPermissions(miniAppId: String) = getCachedRequiredPermissions(miniAppId) +
            getCachedOptionalPermissions(miniAppId)

    /**
     * Returns true if the required permissions are denied, otherwise false.
     * @param [miniAppId] the key provided to find the stored manifest per MiniApp.
     */
    fun isRequiredPermissionDenied(miniAppId: String): Boolean {
        getCachedRequiredPermissions(miniAppId).find {
            it.second != MiniAppCustomPermissionResult.ALLOWED
        }?.let { return true }
        return false
    }

    @VisibleForTesting
    fun getCachedRequiredPermissions(
        miniAppId: String
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        return try {
            val manifest = readMiniAppManifest(miniAppId)
            val cachedPermissions = miniAppCustomPermissionCache.readPermissions(miniAppId).pairValues
            manifest?.requiredPermissions?.mapNotNull { (first) ->
                cachedPermissions.find { it.first == first }
            }!!
        } catch (e: Exception) {
            emptyList()
        }
    }

    @VisibleForTesting
    fun getCachedOptionalPermissions(
        miniAppId: String
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        return try {
            val manifest = readMiniAppManifest(miniAppId)
            val cachedPermissions = miniAppCustomPermissionCache.readPermissions(miniAppId).pairValues
            manifest?.optionalPermissions?.mapNotNull { (first) ->
                cachedPermissions.find { it.first == first }
            }!!
        } catch (e: Exception) {
            emptyList()
        }
    }
}
