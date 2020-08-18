package com.rakuten.tech.mobile.miniapp.js

/** Type of miniapp permission. **/
enum class MiniAppPermissionType(val type: String) {
    UNKNOWN("unknown"),
    LOCATION("location");

    internal companion object {

        internal fun getValue(type: String) = values().find { it.type == type } ?: UNKNOWN
    }
}

/** Type of miniapp custom permission. **/
enum class MiniAppCustomPermissionType(val type: String) {
    USER_NAME("rakuten.miniapp.USER_NAME"),
    PROFILE_PHOTO("rakuten.miniapp.PROFILE_PHOTO"),
    CONTACT_LIST("rakuten.miniapp.CONTACT_LIST");

    internal companion object {

        internal fun getValue(type: String) = values().find { it.type == type }
    }
}

internal enum class MiniAppPermissionResult(val type: String) {
    ALLOWED("Allowed"),
    DENIED("Denied");

    internal companion object {

        internal fun getValue(isGranted: Boolean) = if (isGranted) ALLOWED else DENIED
    }
}
