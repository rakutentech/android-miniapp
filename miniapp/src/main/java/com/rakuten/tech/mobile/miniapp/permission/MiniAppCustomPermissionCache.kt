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
        if (prefs.contains(miniAppId)) {
            return try {
                Gson().fromJson(
                    prefs.getString(miniAppId, ""),
                    object : TypeToken<MiniAppCustomPermission>() {}.type
                )
            } catch (e: Exception) {
                defaultDeniedList(miniAppId)
            }
        }
        return defaultDeniedList(miniAppId)
    }

    /**
     * Stores the grant results to SharedPreferences.
     * @param [miniAppCustomPermission] an object to contain the results per MiniApp.
     */
    fun storePermissions(
        miniAppCustomPermission: MiniAppCustomPermission
    ) {
        val cached = readPermissions(miniAppCustomPermission.miniAppId).pairValues
        val supplied = miniAppCustomPermission.pairValues.toMutableList()

        // Remove any unknown permission parameter from HostApp.
        supplied.removeAll { (first) ->
            first.type == MiniAppCustomPermissionType.UNKNOWN.type
        }

        try {
            val jsonToStore: String = Gson().toJson(
                MiniAppCustomPermission(
                    miniAppCustomPermission.miniAppId,
                    prepareAllPermissionsToStore(cached, supplied)
                )
            )
            prefs.edit().putString(miniAppCustomPermission.miniAppId, jsonToStore).apply()
        } catch (e: Exception) {
            e.message.toString()
        }
    }

    /**
     * Prepares all custom permissions per MiniApp by comparing the cached and supplied
     * custom permissions with replacing old grant results with new grant results.
     */
    @VisibleForTesting
    internal fun prepareAllPermissionsToStore(
        cached: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>,
        supplied: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val combined = (cached + supplied).toMutableList()
        combined.removeAll { (first) ->
            first.type in supplied.groupBy { it.first.type }
        }
        return combined + supplied
    }

    @VisibleForTesting
    internal fun defaultDeniedList(miniAppId: String): MiniAppCustomPermission {
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
                )
            )
        )
    }
}
