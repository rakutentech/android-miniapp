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
import java.io.File

/**
 * A caching class to read and store the [CachedManifest] per MiniApp using [File].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException", "TooManyFunctions")
internal class DownloadedManifestCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.manifest.cache.downloaded", Context.MODE_PRIVATE
    )

    private val sdkBasePath = context.filesDir.path
    private val miniAppBasePath = "$sdkBasePath/$SUB_DIR_MINIAPP/"
    fun getManifestPath(appId: String) = "${miniAppBasePath}$appId/"

    init {
        migrateToFileStorage()
    }

    /**
     * Reads the downloaded manifest from File.
     * @param [miniAppId] the key provided to find the stored manifest per MiniApp.
     * @return [CachedManifest] an object to contain MiniAppManifest per versionId,
     * if data has been stored in cache, otherwise null.
     */
    fun readDownloadedManifest(miniAppId: String): CachedManifest? = readFromCachedFile(miniAppId)

    /**
     * Stores the downloaded manifest to File.
     * @param [miniAppId] the key provided to store manifest per MiniApp id.
     * @return [CachedManifest] an object to contain MiniAppManifest per versionId.
     */
    fun storeDownloadedManifest(
        miniAppId: String,
        cachedManifest: CachedManifest
    ) = storeNewFile(miniAppId, cachedManifest)

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

    private fun toCachedManifest(manifestJsonStr: String): CachedManifest? {
        return try {
            Gson().fromJson(manifestJsonStr, object : TypeToken<CachedManifest>() {}.type)
        } catch (e: Exception) {
            Log.e(this::class.java.canonicalName, e.message.toString())
            null
        }
    }

    private fun migrateToFileStorage() {
        if (prefs.all.isNotEmpty()) {
            prefs.all.forEach {
                storeNewFile(it.key, toCachedManifest(it.value.toString()))
            }
            prefs.edit().clear().apply()
        }
    }

    private fun storeNewFile(miniAppId: String, cachedManifest: CachedManifest?) {
        File(getManifestPath(miniAppId), DEFAULT_FILE_NAME).printWriter().use { out ->
            val jsonToStore: String = Gson().toJson(cachedManifest)
            out.println("$jsonToStore}")
        }
    }

    @VisibleForTesting
    fun readFromCachedFile(miniAppId: String): CachedManifest? {
        val file = File(getManifestPath(miniAppId), DEFAULT_FILE_NAME)
        if (!file.exists()) return null
        return try {
            val jsonToRead = file.bufferedReader()
                    .use {
                        it.readText()
                                .dropLast(2) // for fixing the json format
                    }
            toCachedManifest(jsonToRead)
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        const val DEFAULT_FILE_NAME = "manifest.txt"
        const val SUB_DIR_MINIAPP = "miniapp"
    }
}
