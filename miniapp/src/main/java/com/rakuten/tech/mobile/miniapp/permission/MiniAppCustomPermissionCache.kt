package com.rakuten.tech.mobile.miniapp.permission

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception

/**
 * A caching class to read and store the grant results of custom permissions per MiniApp
 * using [SharedPreferences].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException", "LongMethod")
internal class MiniAppCustomPermissionCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.custom.permissions.cache", Context.MODE_PRIVATE
    )

    /**
     * Reads the grant results from SharedPreferences.
     * @param [miniAppId] the key provided to find the stored results per MiniApp
     * @return [MiniAppCustomPermission] an object to contain the results per MiniApp
     * if data has been stored in cache, otherwise empty value.
     */
    fun readPermissions(miniAppId: String): MiniAppCustomPermission {
        return if (prefs.contains(miniAppId)) {
            try {
                val cachedPermission: MiniAppCustomPermission = Gson().fromJson(
                    prefs.getString(miniAppId, ""),
                    object : TypeToken<MiniAppCustomPermission>() {}.type
                )
                cachedPermission
            } catch (e: Exception) {
                MiniAppCustomPermission(miniAppId, emptyList())
            }
        } else {
            MiniAppCustomPermission(miniAppId, emptyList())
        }
    }

    /**
     * Stores the grant results to SharedPreferences.
     * @param [miniAppCustomPermission] an object to contain the results per MiniApp.
     */
    fun storePermissions(
        miniAppCustomPermission: MiniAppCustomPermission
    ) {
        val supplied = miniAppCustomPermission.pairValues.toMutableList()
        // Remove any unknown permission parameter from HostApp.
        supplied.removeAll { (first) ->
            first.type == MiniAppCustomPermissionType.UNKNOWN.type
        }
        val miniAppId = miniAppCustomPermission.miniAppId
        val allPermissions = prepareAllPermissionsToStore(miniAppId, supplied)
        applyStoringPermissions(MiniAppCustomPermission(miniAppId, allPermissions))
    }

    fun removePermissionsNotMatching(
        miniAppId: String,
        permissions: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
    ) {
        val cachedPermissions = readPermissions(miniAppId).pairValues.toMutableList()
        val newPermissions = cachedPermissions.mapNotNull { (first) ->
            permissions.find { it.first == first }
        }
        applyStoringPermissions(MiniAppCustomPermission(miniAppId, newPermissions))
    }

    /**
     * Remove the grant results per MiniApp from SharedPreferences.
     * @param [miniAppId] the key provided to find the stored results per MiniApp.
     */
    fun removePermission(miniAppId: String) {
        prefs.edit().remove(miniAppId).apply()
    }

    @VisibleForTesting
    fun applyStoringPermissions(miniAppCustomPermission: MiniAppCustomPermission) {
        val jsonToStore: String = Gson().toJson(sortedByDefault(miniAppCustomPermission))
        prefs.edit().putString(miniAppCustomPermission.miniAppId, jsonToStore).apply()
    }

    // Sort the `pairValues` by ordinal of [MiniAppCustomPermissionType].
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun sortedByDefault(miniAppCustomPermission: MiniAppCustomPermission): MiniAppCustomPermission {
        val sortedPairValues = miniAppCustomPermission.pairValues.sortedBy { it.first.ordinal }
        return MiniAppCustomPermission(miniAppCustomPermission.miniAppId, sortedPairValues)
    }

    @VisibleForTesting
    fun prepareAllPermissionsToStore(
        miniAppId: String,
        supplied: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        // retrieve permissions by comparing cached and supplied (from HostApp) permissions
        // readPermissions already filters the changed permissions from defaultDeniedList
        val cached = readPermissions(miniAppId).pairValues
        val combined = (cached + supplied).toMutableList()
        combined.removeAll { (first) ->
            first.type in supplied.groupBy { it.first.type }
        }
        return combined + supplied
    }

    /**
     * Check if any specific custom permission has been allowed or denied.
     * @param [miniAppId] the key provided to find the stored results per MiniApp.
     * @param [type] MiniAppCustomPermissionType to check.
     * @return [Boolean] True if the permission has been allowed, otherwise false.
     */
    fun hasPermission(miniAppId: String, type: MiniAppCustomPermissionType): Boolean {
        var isPermissionGranted = false
        readPermissions(miniAppId).pairValues.find {
            it.first == type && it.second == MiniAppCustomPermissionResult.ALLOWED
        }?.let { isPermissionGranted = true }

        return isPermissionGranted
    }
}
