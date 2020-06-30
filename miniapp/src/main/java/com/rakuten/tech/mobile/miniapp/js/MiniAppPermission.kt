package com.rakuten.tech.mobile.miniapp.js

import android.Manifest

internal object MiniAppPermission {

    internal object Code {
        const val GEOLOCATION = 1001
    }

    internal object PermissionType {
        const val GEOLOCATION = "miniapp.permission.geolocation"
    }

    internal fun getPermissionRequest(permissionType: String) = when (permissionType) {
        PermissionType.GEOLOCATION -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        else -> emptyArray()
    }

    internal fun getRequestCode(permissionType: String) = when (permissionType) {
        PermissionType.GEOLOCATION -> Code.GEOLOCATION
        else -> 0
    }
}
