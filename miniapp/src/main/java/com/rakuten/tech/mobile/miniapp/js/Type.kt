package com.rakuten.tech.mobile.miniapp.js

internal enum class ActionType(val action: String) {
    GET_UNIQUE_ID("getUniqueId"),
    REQUEST_PERMISSION("requestPermission"),
    REQUEST_CUSTOM_PERMISSIONS("requestCustomPermissions"),
    SHARE_INFO("shareInfo"),

    LOAD_AD("loadAd"),
    SHOW_AD("showAd")
}

internal enum class DialogType {
    ALERT,
    CONFIRM,
    PROMPT,
}

internal enum class AdType(val value: Int) {
    INTERSTITIAL(0),
    REWARDED(1),
}
