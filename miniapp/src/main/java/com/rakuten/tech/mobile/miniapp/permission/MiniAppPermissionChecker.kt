package com.rakuten.tech.mobile.miniapp.permission

import android.content.Context
import android.content.SharedPreferences

class MiniAppPermissionChecker(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "com.rakuten.tech.mobile.miniapp.permissions", Context.MODE_PRIVATE
    )

    internal fun checkPermission(permission: String): Boolean {
        return prefs.getBoolean(permission, false)
    }

    internal fun setPermissionResult(
        permission: String,
        grantResult: Boolean
    ) {
        HashMap<String, Boolean>().apply {
            put(permission, grantResult)
            entries.forEach {
                prefs.edit().putBoolean(it.key, it.value).apply()
            }
        }
    }
}
