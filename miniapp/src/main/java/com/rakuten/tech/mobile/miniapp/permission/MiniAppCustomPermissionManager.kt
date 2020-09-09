package com.rakuten.tech.mobile.miniapp.permission

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.js.CustomPermissionObj

/**
 * A class to manage additional features involved with custom permissions in this SDK.
 */
internal class MiniAppCustomPermissionManager {

    /**
     * Prepares a list of custom permissions Pair with names and description.
     * @param [permissionObjList] list of CustomPermissionObj.
     * @return [List<Pair<MiniAppCustomPermissionType, String>>].
     */
    fun preparePermissionsWithDescription(
        permissionObjList: ArrayList<CustomPermissionObj>
    ): List<Pair<MiniAppCustomPermissionType, String>> {
        val permissionsWithDescription =
            arrayListOf<Pair<MiniAppCustomPermissionType, String>>()
        permissionObjList.forEach {
            MiniAppCustomPermissionType.getValue(it.name).let { type ->
                permissionsWithDescription.add(Pair(type, it.description))
            }
        }
        return permissionsWithDescription
    }

    /**
     * Creates a JSON string by mapping with [MiniAppCustomPermissionResponse] class.
     * @param [context] for using in SDK cache.
     * @param [miniAppId] list of custom permissions Pair.
     * @param [suppliedPermissions] list of custom permissions Pair with names and description,
     * initially it was prepared in [MiniAppMessageBridge].
     * @return [String] as json response with permission grant results.
     */
    fun createJsonResponse(
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        miniAppId: String,
        suppliedPermissions: List<Pair<MiniAppCustomPermissionType, String>>
    ): String {
        val responseObj = MiniAppCustomPermissionResponse(arrayListOf())
        val permissions =
            filterPermissionsToSend(miniAppCustomPermissionCache, miniAppId, suppliedPermissions)
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

    /**
     * Filters supplied custom permissions from the cache per MiniApp;
     * By default, PERMISSION_NOT_AVAILABLE will be added if there is
     * any unknown permission.
     */
    @VisibleForTesting
    fun filterPermissionsToSend(
        miniAppCustomPermissionCache: MiniAppCustomPermissionCache,
        miniAppId: String,
        suppliedPermissions: List<Pair<MiniAppCustomPermissionType, String>>
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val cachedPermissions = miniAppCustomPermissionCache.readPermissions(miniAppId).pairValues
        val filteredPair =
            mutableListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

        suppliedPermissions.forEach { (first) ->
            cachedPermissions.find {
                it.first == first
            }?.let { filteredPair.add(it) }

            // Add PERMISSION_NOT_AVAILABLE if there is any unknown permission parameter
            // sent from HostApp.
            if (first.type == MiniAppCustomPermissionType.UNKNOWN.type)
                filteredPair.add(defaultUnknownPermissionPair)
        }

        return filteredPair
    }

    private val defaultUnknownPermissionPair = Pair(
        MiniAppCustomPermissionType.UNKNOWN,
        MiniAppCustomPermissionResult.PERMISSION_NOT_AVAILABLE
    )
}
