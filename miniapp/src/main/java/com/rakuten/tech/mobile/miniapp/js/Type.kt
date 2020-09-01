package com.rakuten.tech.mobile.miniapp.js

internal enum class ActionType(val action: String) {
    GET_UNIQUE_ID("getUniqueId"),
    REQUEST_PERMISSION("requestPermission"),
    SHARE_CONTENT("shareContent")
}

internal enum class DialogType {
    ALERT,
    CONFIRM,
    PROMPT,
}
