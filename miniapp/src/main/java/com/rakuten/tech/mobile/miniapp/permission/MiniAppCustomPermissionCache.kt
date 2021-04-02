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
                val nonNullList = cachedPermission.pairValues.filterNot { it.first == null }
                cachedPermission.copy(pairValues = nonNullList)
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
        supplied.removeAll { (first) -> first.type == MiniAppCustomPermissionType.UNKNOWN.type }
        val miniAppId = miniAppCustomPermission.miniAppId
        val allPermissions = prepareAllPermissionsToStore(miniAppId, supplied)
        applyStoringPermissions(MiniAppCustomPermission(miniAppId, allPermissions))
    }

    /**
     * Remove permissions from cache which are not matched with the supplied [permissions].
     * @param [miniAppId] the key provided to find the stored results per MiniApp.
     * @param [permissions] the custom permissions to match with the cached permissions.
     */
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

    /**
     * Prepare all the permissions supplied from HostApp including the permissions which was already
     * cached. It removes the old cached results if HostApp supplies the new results.
     * @param [miniAppId] the key provided to find the cached results per MiniApp.
     * @param [supplied] the custom permissions sent by Host App.
     */
    @VisibleForTesting
    fun prepareAllPermissionsToStore(
        miniAppId: String,
        supplied: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        // retrieve all permissions by comparing cached and supplied (from HostApp) permissions
        val set = mutableSetOf<MiniAppCustomPermissionType>()
        val returnList = mutableListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        supplied.forEach {
            set.add(it.first)
            returnList.add(it)
        }
        readPermissions(miniAppId).pairValues.forEach {
            if (!set.contains(it.first))
                returnList.add(it)
        }

        return returnList
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
