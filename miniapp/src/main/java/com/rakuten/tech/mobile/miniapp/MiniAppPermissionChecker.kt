package com.rakuten.tech.mobile.miniapp

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class MiniAppPermissionChecker {

    // TODO: PermissionChecker
    internal fun selfCheckPermission(activity: Activity, permission: String): Boolean {
        // TODO: verify permission by api or local cache
        var prefs: SharedPreferences = activity.getSharedPreferences(
            "com.rakuten.tech.mobile.miniapp_permission_preferences", Context.MODE_PRIVATE
        )

        // default grant permission is false
        return prefs.getBoolean(permission, false)
    }

    internal fun checkPermission(activity: Activity, permission: String): Boolean {
        // TODO: verify permission by api or local cache
        var prefs: SharedPreferences = activity.getSharedPreferences(
            "com.rakuten.tech.mobile.miniapp_permission_preferences", Context.MODE_PRIVATE
        )

        // TODO: default grant permission is true in UI
        return prefs.getBoolean(permission, true)
    }

    internal fun updatePermissionResult(
        activity: Activity,
        permission: String,
        grantResult: Boolean
    ) {
        // TODO: update permission result in cache
        val prefs: SharedPreferences = activity.getSharedPreferences(
            "com.rakuten.tech.mobile.miniapp_permission_preferences", Context.MODE_PRIVATE
        )

        val grantResult = HashMap<String, Boolean>().apply {
            put(permission, grantResult)
        }

        for ((key, value) in grantResult.entries) {
            prefs.edit().putBoolean(key, value).apply()
        }
    }
}
