package com.rakuten.tech.mobile.miniapp.js

internal enum class ActionType(val action: String) {
    GET_UNIQUE_ID("getUniqueId"),
    REQUEST_PERMISSION("requestPermission"),
    REQUEST_CUSTOM_PERMISSIONS("requestCustomPermissions"),
    SHARE_INFO("shareInfo"),
    LOAD_AD("loadAd"),
    SHOW_AD("showAd"),
    GET_USER_NAME("getUserName"),
    GET_PROFILE_PHOTO("getProfilePhoto"),
    GET_ACCESS_TOKEN("getAccessToken"),
    SET_SCREEN_ORIENTATION("setScreenOrientation"),
    GET_CONTACTS("getContacts"),
    SEND_MESSAGE_TO_CONTACT("sendMessageToContact"),
    SEND_MESSAGE_TO_CONTACT_ID("sendMessageToContactId"),
    SEND_MESSAGE_TO_MULTIPLE_CONTACTS("sendMessageToMultipleContacts"),
    GET_POINTS("getPoints"),
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

internal enum class ScreenOrientation(val value: String) {
    LOCK_PORTRAIT("rakuten.miniapp.screen.LOCK_PORTRAIT"),
    LOCK_LANDSCAPE("rakuten.miniapp.screen.LOCK_LANDSCAPE"),
    LOCK_RELEASE("rakuten.miniapp.screen.LOCK_RELEASE"),
}

/** Types of native events can be dispatched to miniapp. **/
enum class NativeEventType(val value: String) {
    EXTERNAL_WEBVIEW_CLOSE("miniAppWebviewClosed"),
    MINIAPP_ON_PAUSE("miniAppOnPause")
}
