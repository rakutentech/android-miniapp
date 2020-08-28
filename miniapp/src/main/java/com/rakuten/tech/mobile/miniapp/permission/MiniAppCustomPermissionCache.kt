package com.rakuten.tech.mobile.miniapp.permission

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * A class to read and store the grant results of custom permissions per MiniApp.
 */
internal class MiniAppCustomPermissionCache(context: Context) {
    @VisibleForTesting
    val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.custom.permissions.cache", Context.MODE_PRIVATE
    )

    /**
     * Reads the grant results from SharedPreferences.
     * @param [miniAppId] the key provided to find the stored results per MiniApp
     * @return [MiniAppCustomPermission] an object to contain the results per MiniApp.
     */
    fun readPermissions(miniAppId: String): MiniAppCustomPermission {
        if (prefs.contains(miniAppId)) {
            return Gson().fromJson(
                prefs.getString(miniAppId, ""),
                object : TypeToken<MiniAppCustomPermission>() {}.type
            )
        }

        // return default values if there is no data stored per miniAppId
        return MiniAppCustomPermission(
            miniAppId,
            defaultDeniedList
        )
    }

    /**
     * Stores the grant results to SharedPreferences.
     * @param [miniAppCustomPermission] an object to contain the results per MiniApp.
     * @return [String] JSON string response provides custom permission names and
     * the corresponding grant results to the HostApp.
     */
    fun storePermissions(
        miniAppCustomPermission: MiniAppCustomPermission
    ): String {
        val json: String = Gson().toJson(
            MiniAppCustomPermission(
                miniAppCustomPermission.miniAppId,
                combineAllPermissionsToStore(miniAppCustomPermission)
            )
        )
        prefs.edit().putString(miniAppCustomPermission.miniAppId, json).apply()
        return toJsonResponse(filterSuppliedPermissionsToSend(miniAppCustomPermission))
    }

    /**
     * Combines all custom permissions per MiniApp by comparing the cached and supplied
     * custom permissions with replacing old grant results with new grant results.
     * @param [miniAppCustomPermission] an object to contain the results per MiniApp.
     * @return [MiniAppCustomPermission.pairValues].
     */
    private fun combineAllPermissionsToStore(
        miniAppCustomPermission: MiniAppCustomPermission
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val cached = readPermissions(miniAppCustomPermission.miniAppId).pairValues
        val supplied = miniAppCustomPermission.pairValues
        val combined = (cached + supplied).toMutableList()
        combined.removeAll { (first) ->
            first.type in supplied.groupBy { it.first.type }
        }
        return combined + supplied
    }

    /**
     * Filters supplied custom permissions from all cached custom permissions per MiniApp.
     * @param [miniAppCustomPermission] an object to contain the results per MiniApp.
     * @return [MiniAppCustomPermission.pairValues].
     */
    private fun filterSuppliedPermissionsToSend(
        miniAppCustomPermission: MiniAppCustomPermission
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val cached = readPermissions(miniAppCustomPermission.miniAppId).pairValues
        val supplied = miniAppCustomPermission.pairValues
        val filteredPair =
            mutableListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        supplied.forEach { (first) ->
            filteredPair.addAll(cached.filter {
                first.type == it.first.type
            })
        }
        return filteredPair
    }

    /**
     * Provides a JSON string by mapping with [MiniAppCustomPermissionResponse] class
     * @param [permissions] list of custom permissions Pair.
     * @return [String].
     */
    private fun toJsonResponse(
        permissions: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
    ): String {
        val responseObj =
            MiniAppCustomPermissionResponse(
                arrayListOf()
            )
        permissions.forEach {
            responseObj.permissions.add(
                MiniAppCustomPermissionResponse.CustomPermissionResponseObj(
                    it.first.type,
                    it.second.name
                )
            )
        }
        return Gson().toJson(responseObj).toString()
    }

    private companion object {
        val defaultDeniedList = listOf(
            Pair(
                MiniAppCustomPermissionType.USER_NAME,
                MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
            ),
            Pair(
                MiniAppCustomPermissionType.CONTACT_LIST,
                MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
            ),
            Pair(
                MiniAppCustomPermissionType.PROFILE_PHOTO,
                MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
            )
        )
    }
}
