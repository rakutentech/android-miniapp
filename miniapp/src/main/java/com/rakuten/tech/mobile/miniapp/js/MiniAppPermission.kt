package com.rakuten.tech.mobile.miniapp.js

import android.Manifest
import android.content.pm.PackageManager

/** The resource of miniapp permission. **/
object MiniAppPermission {

    /** Request code resource for permission request. **/
    object ReqCode {
        const val GEOLOCATION = 1001
    }

    internal object PermissionType {
        const val GEOLOCATION = "miniapp.permission.geolocation"
    }

    internal object PermissionResult {
        const val GRANTED = "Granted"
        const val DENIED = "Denied"
    }

    /** Get the correspond permission request code of permission type request from miniapp. **/
    fun getRequestCode(permissionType: String) = when (permissionType) {
        PermissionType.GEOLOCATION -> ReqCode.GEOLOCATION
        else -> 0
    }

    internal fun getPermissionRequest(permissionType: String) = when (permissionType) {
        PermissionType.GEOLOCATION -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        else -> emptyArray()
    }

    internal fun getPermissionResult(grantResult: Int) = when (grantResult) {
        PackageManager.PERMISSION_GRANTED -> PermissionResult.GRANTED
        else -> PermissionResult.DENIED
    }
}
