package com.rakuten.tech.mobile.miniapp.js

/** Type of miniapp permission. **/
enum class MiniAppPermissionType(val type: String) {
    UNKNOWN("unknown"),
    LOCATION("location"),
    ACCEPT_USER("accept_user");

    internal companion object {

        internal fun getValue(type: String) = values().find { it.type == type } ?: UNKNOWN
    }
}

internal enum class MiniAppPermissionResult(val type: String) {
    ALLOWED("Allowed"),
    DENIED("Denied");

    internal companion object {

        internal fun getValue(isGranted: Boolean) = if (isGranted) ALLOWED else DENIED
    }
}
