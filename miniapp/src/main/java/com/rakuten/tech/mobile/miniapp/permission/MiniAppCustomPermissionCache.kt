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
     * if data has been stored in cache, otherwise default value.
     */
    fun readPermissions(miniAppId: String): MiniAppCustomPermission {
        val defaultValue = defaultDeniedList(miniAppId)
        return if (prefs.contains(miniAppId)) {
            try {
                val cachedPermission: MiniAppCustomPermission = Gson().fromJson(
                    prefs.getString(miniAppId, ""),
                    object : TypeToken<MiniAppCustomPermission>() {}.type
                )
                val cachedPairs = cachedPermission.pairValues.toMutableList()

                // detect any new change with comparing cached permissions and defaultDeniedList
                // change means added new permission / removed existing permission from defaultDeniedList
                val defaultPairs = defaultValue.pairValues
                val changedPermissions = (defaultPairs + cachedPairs).groupBy { it.first.type }
                    .filter { it.value.size == 1 }
                    .flatMap { it.value }

                return if (changedPermissions.isNotEmpty()) {
                    if (cachedPairs.size < defaultPairs.size) {
                        val filteredValue =
                            MiniAppCustomPermission(miniAppId, cachedPairs + changedPermissions)
                        applyStoringPermissions(filteredValue)
                        filteredValue
                    } else {
                        cachedPairs.removeAll { (first) ->
                            first.type in changedPermissions.groupBy { it.first.type }
                        }
                        val filteredValue = MiniAppCustomPermission(miniAppId, cachedPairs)
                        applyStoringPermissions(filteredValue)
                        filteredValue
                    }
                } else {
                    val filteredValue = MiniAppCustomPermission(miniAppId, cachedPairs)
                    applyStoringPermissions(filteredValue)
                    filteredValue
                }
            } catch (e: Exception) {
                // if there is any exception, just return the default value
                defaultValue
            }
        } else {
            // if value hasn't been found in SharedPreferences, save the value
            applyStoringPermissions(defaultValue)
            defaultValue
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

    /**
     * Apply to be saved the grant results as json string to SharedPreferences.
     * @param [miniAppCustomPermission] an object to contain the results per MiniApp.
     */
    @VisibleForTesting
    fun applyStoringPermissions(miniAppCustomPermission: MiniAppCustomPermission) {
        try {
            val jsonToStore: String = Gson().toJson(miniAppCustomPermission)
            prefs.edit().putString(miniAppCustomPermission.miniAppId, jsonToStore).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Prepares all custom permissions per MiniApp by comparing the cached and supplied
     * custom permissions with replacing old grant results with new grant results.
     */
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
     * Update this default list when adding or removing a custom permission,
     * [MiniAppCustomPermissionCache] should automatically handle the value.
     */
    @VisibleForTesting
    fun defaultDeniedList(miniAppId: String): MiniAppCustomPermission {
        return MiniAppCustomPermission(
            miniAppId,
            listOf(
                Pair(
                    MiniAppCustomPermissionType.USER_NAME,
                    MiniAppCustomPermissionResult.DENIED
                ),
                Pair(
                    MiniAppCustomPermissionType.PROFILE_PHOTO,
                    MiniAppCustomPermissionResult.DENIED
                ),
                Pair(
                    MiniAppCustomPermissionType.CONTACT_LIST,
                    MiniAppCustomPermissionResult.DENIED
                ),
                Pair(
                    MiniAppCustomPermissionType.LOCATION,
                    MiniAppCustomPermissionResult.DENIED
                )
            )
        )
    }
}
