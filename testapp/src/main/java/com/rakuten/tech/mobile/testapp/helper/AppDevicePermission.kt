package com.rakuten.tech.mobile.testapp.helper

import android.Manifest
import com.rakuten.tech.mobile.miniapp.permission.MiniAppDevicePermissionType

object AppDevicePermission {

    object ReqCode {
        const val LOCATION = 1001
        const val CAMERA = 1002
    }

    fun getDeviceRequestCode(permissionType: MiniAppDevicePermissionType) = when (permissionType) {
        MiniAppDevicePermissionType.LOCATION -> ReqCode.LOCATION
        MiniAppDevicePermissionType.CAMERA -> ReqCode.CAMERA
        else -> 0
    }

    fun getDevicePermissionRequest(permissionType: MiniAppDevicePermissionType) =
        when (permissionType) {
            MiniAppDevicePermissionType.LOCATION -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            MiniAppDevicePermissionType.CAMERA -> arrayOf(Manifest.permission.CAMERA)
            else -> emptyArray()
        }
}
