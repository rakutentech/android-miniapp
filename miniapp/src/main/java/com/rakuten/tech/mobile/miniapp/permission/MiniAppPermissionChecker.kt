package com.rakuten.tech.mobile.miniapp.permission

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting

/**
 * A class to check if a requested permission has been granted or denied with caching
 * the values of the grant result per permission.
 */
internal class MiniAppPermissionChecker(context: Context) {
    @VisibleForTesting
    val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.permissions", Context.MODE_PRIVATE
    )

    /**
     * Reads a Boolean from SharedPreferences
     * @param [permission] the key provided to find the stored grant result
     * @return [Boolean] the grant result of permission requested if found, if not returns false
     */
    fun checkPermission(permission: String): Boolean {
        return prefs.getBoolean(permission, false)
    }

    /**
     * Caches the Boolean to SharedPreferences
     * @param [permission] the key provided to store the [grantResult] as Boolean
     */
    fun storePermissionResult(
        permission: String,
        grantResult: Boolean
    ) {
        HashMap<String, Boolean>().apply {
            put(permission, grantResult)
            entries.forEach {
                with(prefs.edit()) {
                    putBoolean(it.key, it.value).apply()
                }
            }
        }
    }
}
