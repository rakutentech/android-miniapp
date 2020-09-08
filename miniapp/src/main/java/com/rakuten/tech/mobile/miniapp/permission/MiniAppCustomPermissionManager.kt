package com.rakuten.tech.mobile.miniapp.permission

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.rakuten.tech.mobile.miniapp.MiniApp

/**
 * A class to manage additional features involved with custom permissions in this SDK.
 */
class MiniAppCustomPermissionManager(val miniapp: MiniApp) {

    /**
     * Creates a JSON string by mapping with [MiniAppCustomPermissionResponse] class.
     * @param [miniAppId] list of custom permissions Pair.
     * @param [suppliedPermissions] list of custom permissions Pair with names and description,
     * initially it was prepared in [MiniAppMessageBridge].
     * @return [String] as json response with permission grant results.
     */
    fun createJsonResponse(
        miniAppId: String,
        suppliedPermissions: List<Pair<MiniAppCustomPermissionType, String>>
    ): String {
        val responseObj = MiniAppCustomPermissionResponse(arrayListOf())
        val permissions = filterPermissionsToSend(miniAppId, suppliedPermissions)
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
    internal fun filterPermissionsToSend(
        miniAppId: String,
        suppliedPermissions: List<Pair<MiniAppCustomPermissionType, String>>
    ): List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>> {
        val cachedPermissions = miniapp.getCustomPermissions(miniAppId).pairValues
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
