package com.rakuten.tech.mobile.miniapp.js

import android.Manifest

internal object MiniAppPermission {

    internal const val GEOLOCATION: Int = 1001

    internal fun getPermissionRequest(permissionType: Int) = when (permissionType) {
        GEOLOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
        else -> ""
    }
}
