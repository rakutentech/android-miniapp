package com.rakuten.tech.mobile.miniapp.js

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.MiniApp.Companion.CUSTOM_PERMISSION_DENIED

/**
 * A class to check if a requested permission has been granted or rejected with storing
 * the values of the grant result per permission.
 */
internal class MiniAppCustomPermissionCache(context: Context) {
    @VisibleForTesting
    val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.permissions", Context.MODE_PRIVATE
    )

    /**
     * Reads grant result from SharedPreferences.
     * @param [permissions] the key-set provided to find the stored corresponding grant results.
     * @return [List<String>] the grant results of permissions requested.
     */
    fun readPermissions(permissions: List<String>): List<String> {
        val grantResults = mutableListOf<String>()

        permissions.forEach { key ->
            prefs.getString(key, CUSTOM_PERMISSION_DENIED)?.let { grantResults.add(it) }
        }

        return grantResults
    }

    /**
     * Stores grant result to SharedPreferences.
     * @param [permissions] as a key provided to store the [grantResults] as String.
     */
    fun storePermissionResults(
        permissions: List<String>,
        grantResults: List<String>
    ) {
        permissions.forEachIndexed { index, key ->
            prefs.edit().putString(key, grantResults[index]).apply()
        }
    }
}
