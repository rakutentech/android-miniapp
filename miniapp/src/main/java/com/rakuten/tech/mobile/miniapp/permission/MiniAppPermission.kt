package com.rakuten.tech.mobile.miniapp.permission

/** Type of miniapp permission. **/
enum class MiniAppPermissionType(val type: String, val platform: String) {
    UNKNOWN("unknown", ""),
    LOCATION("location", MiniAppPermissionPlatform.Android.name);

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

/** Type of platform for requesting permission. **/
enum class MiniAppPermissionPlatform {
    Android,
    MiniApp
}
