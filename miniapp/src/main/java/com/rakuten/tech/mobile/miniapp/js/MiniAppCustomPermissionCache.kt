package com.rakuten.tech.mobile.miniapp.js

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
     * @return [MiniAppCustomPermission] an object to hold the results per MiniApp.
     */
    fun readPermissions(miniAppId: String): MiniAppCustomPermission {
        if (prefs.contains(miniAppId)) {
            return Gson().fromJson(
                prefs.getString(miniAppId, ""),
                object : TypeToken<MiniAppCustomPermission>() {}.type
            )
        }

        // if there is no data per miniAppId, then return default values
        return MiniAppCustomPermission(miniAppId, defaultDeniedList)
    }

    /**
     * Stores the grant results to SharedPreferences.
     * @param [miniAppCustomPermission] an object to hold the results per MiniApp.
     */
    fun storePermissions(
        miniAppCustomPermission: MiniAppCustomPermission
    ) {
        val json: String = Gson().toJson(
            MiniAppCustomPermission(
                miniAppCustomPermission.miniAppId,
                filterValuesToStore(miniAppCustomPermission)
            )
        )
        prefs.edit().putString(miniAppCustomPermission.miniAppId, json).apply()
    }

    private fun filterValuesToStore(
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
