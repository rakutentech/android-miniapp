package com.rakuten.tech.mobile.miniapp.js

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * A class to check if a requested permission has been granted or rejected with storing
 * the values of the grant result per permission.
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
        return MiniAppCustomPermission(
            miniAppId,
            listOf(
                Pair(MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED),
                Pair(
                    MiniAppCustomPermissionType.CONTACT_LIST,
                    MiniAppCustomPermissionResult.DENIED
                ),
                Pair(
                    MiniAppCustomPermissionType.PROFILE_PHOTO,
                    MiniAppCustomPermissionResult.DENIED
                )
            )
        )
    }

    /**
     * Stores the grant results to SharedPreferences.
     * @param [miniAppCustomPermission] an object to hold the key and custom permission data
     */
    fun storePermissions(
        miniAppCustomPermission: MiniAppCustomPermission
    ) {
        val json: String = Gson().toJson(miniAppCustomPermission)
        prefs.edit().putString(miniAppCustomPermission.miniAppId, json).apply()
    }
}
