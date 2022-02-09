package com.rakuten.tech.mobile.miniapp.permission

import androidx.annotation.Keep

/** Type of miniapp device permission. **/
enum class MiniAppDevicePermissionType(val type: String) {
    UNKNOWN("unknown"),
    LOCATION("location"),
    CAMERA("camera");

    internal companion object {

        internal fun getValue(type: String) = values().find { it.type == type } ?: UNKNOWN
    }
}

/** Type of miniapp custom permission. **/
@Keep
enum class MiniAppCustomPermissionType(val type: String) {
    USER_NAME("rakuten.miniapp.user.USER_NAME"),
    PROFILE_PHOTO("rakuten.miniapp.user.PROFILE_PHOTO"),
    CONTACT_LIST("rakuten.miniapp.user.CONTACT_LIST"),
    ACCESS_TOKEN("rakuten.miniapp.user.ACCESS_TOKEN"),
    SEND_MESSAGE("rakuten.miniapp.user.action.SEND_MESSAGE"),
    LOCATION("rakuten.miniapp.device.LOCATION"),
    POINTS("rakuten.miniapp.user.POINTS"),
    FILE_DOWNLOAD("rakuten.miniapp.device.FILE_DOWNLOAD"),
    UNKNOWN("UNKNOWN");

    internal companion object {

        internal fun getValue(type: String) = values().find { it.type == type } ?: UNKNOWN
    }
}

internal enum class MiniAppDevicePermissionResult(val type: String) {
    ALLOWED("Allowed"),
    DENIED("Denied");

    internal companion object {

        internal fun getValue(isGranted: Boolean) = if (isGranted) ALLOWED else DENIED
    }
}

/** Type of miniapp custom permission result. **/
@Keep
enum class MiniAppCustomPermissionResult {
    ALLOWED,
    DENIED,
    PERMISSION_NOT_AVAILABLE;
}
