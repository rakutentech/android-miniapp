package com.rakuten.tech.mobile.testapp.helper

import android.Manifest
import com.rakuten.tech.mobile.miniapp.js.MiniAppPermissionType

object AppPermission {

    object ReqCode {
        const val LOCATION = 1001
    }

    fun getRequestCode(permissionType: MiniAppPermissionType) = when (permissionType) {
        MiniAppPermissionType.LOCATION -> ReqCode.LOCATION
        else -> 0
    }

    fun getPermissionRequest(permissionType: MiniAppPermissionType) = when (permissionType) {
        MiniAppPermissionType.LOCATION -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        else -> emptyArray()
    }
}
