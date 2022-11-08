package com.rakuten.tech.mobile.testapp.ui.permission

import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType

fun MiniAppCustomPermissionType?.toReadableName(): String {
    return when (this) {
        MiniAppCustomPermissionType.USER_NAME -> "User Name"
        MiniAppCustomPermissionType.PROFILE_PHOTO -> "Profile Photo"
        MiniAppCustomPermissionType.CONTACT_LIST -> "Contact List"
        MiniAppCustomPermissionType.ACCESS_TOKEN -> "Access Token"
        MiniAppCustomPermissionType.SEND_MESSAGE -> "Send Message"
        MiniAppCustomPermissionType.LOCATION -> "Device Location"
        MiniAppCustomPermissionType.POINTS -> "Rakuten Points"
        MiniAppCustomPermissionType.FILE_DOWNLOAD -> "File Download"
        else -> "Unknown"
    }
}
