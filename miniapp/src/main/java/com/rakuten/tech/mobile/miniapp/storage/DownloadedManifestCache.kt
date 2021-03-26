package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import kotlin.Exception

/**
 * A caching class to read and store the [CachedManifest] per MiniApp using [SharedPreferences].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal class DownloadedManifestCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.manifest.cache.downloaded", Context.MODE_PRIVATE
    )

    /**
     * Reads the downloaded manifest from SharedPreferences.
     * @param [miniAppId] the key provided to find the stored manifest per MiniApp.
     * @return [CachedManifest] an object to contain MiniAppManifest per versionId,
     * if data has been stored in cache, otherwise null.
     */
    fun readDownloadedManifest(miniAppId: String): CachedManifest? {
        val manifestJsonStr =
            prefs.getString(miniAppId, null) ?: return null
        return try {
            Gson().fromJson(manifestJsonStr, object : TypeToken<CachedManifest>() {}.type)
        } catch (e: Exception) {
            Log.e(this::class.java.canonicalName, e.message.toString())
            null
        }
    }

    /**
     * Stores the downloaded manifest to SharedPreferences.
     * @param [miniAppId] the key provided to store manifest per MiniApp id.
     * @return [CachedManifest] an object to contain MiniAppManifest per versionId.
     */
    fun storeDownloadedManifest(
        miniAppId: String,
        cachedManifest: CachedManifest
    ) {
        val jsonToStore: String = Gson().toJson(cachedManifest)
        prefs.edit().putString(miniAppId, jsonToStore).apply()
    }

    /**
     * Returns the list of all manifest permissions e.g. required and optional.
     */
    fun getAllPermissions(cachedPermissions: MiniAppCustomPermission) =
        getRequiredPermissions(cachedPermissions) + getOptionalPermissions(cachedPermissions)

    /**
     * Returns true if the required permissions are denied, otherwise false.
     */
    fun isRequiredPermissionDenied(
        cachedPermissions: MiniAppCustomPermission
    ): Boolean {
        getRequiredPermissions(cachedPermissions).find {
            it.second != MiniAppCustomPermissionResult.ALLOWED
        }?.let {
            return true
        }

        return false
    }

    @VisibleForTesting
    fun getRequiredPermissions(
        cachedPermissions: MiniAppCustomPermission
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        return try {
            val cachedManifest = readDownloadedManifest(cachedPermissions.miniAppId)
            cachedManifest?.miniAppManifest?.requiredPermissions?.mapNotNull { (first) ->
                cachedPermissions.pairValues.find { it.first == first }
            }!!
        } catch (e: Exception) {
            emptyList()
        }
    }

    @VisibleForTesting
    fun getOptionalPermissions(
        cachedPermissions: MiniAppCustomPermission
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        return try {
            val cachedManifest = readDownloadedManifest(cachedPermissions.miniAppId)
            cachedManifest?.miniAppManifest?.optionalPermissions?.mapNotNull { (first) ->
                cachedPermissions.pairValues.find { it.first == first }
            }!!
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAccessTokenPermissions(miniAppId: String): List<AccessTokenScope> {
        val manifest: CachedManifest? = readDownloadedManifest(miniAppId)
        return manifest?.miniAppManifest?.accessTokenPermissions ?: emptyList()
    }
}
