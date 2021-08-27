package com.rakuten.tech.mobile.miniapp.analytics

internal enum class Etype(val value: String) {
    APPEAR("appear"),
    CLICK("click"),
}

internal enum class Actype(val value: String) {
    SEND_MESSAGE_TO_CONTACT_ID("mini_app_sendMessageToContactId"),
    HOST_LAUNCH("mini_app_host_launch"),
    OPEN("mini_app_open"),
    CLOSE("mini_app_close"),
    GET_USER_NAME("mini_app_getUserName"),
    SET_SCREEN_ORIENTATION("mini_app_setScreenOrientation"),
    GET_CONTACTS("mini_app_getContacts"),
    SEND_MESSAGE_TO_CONTACT("mini_app_sendMessageToContact"),
    REQUEST_PERMISSION("mini_app_requestPermission"),
    REQUEST_CUSTOM_PERMISSIONS("mini_app_requestCustomPermissions"),
    SHARE_INFO("mini_app_shareInfo"),
    LOAD_AD("mini_app_loadAd"),
    SHOW_AD("mini_app_showAd"),
    GET_POINTS("mini_app_getPoints"),
    GET_PROFILE_PHOTO("mini_app_getProfilePhoto"),
    GET_ACCESS_TOKEN("mini_app_getAccessToken"),
    SEND_MESSAGE_TO_MULTIPLE_CONTACTS("mini_app_sendMessageToMultipleContacts"),
    GET_UNIQUE_ID("mini_app_getUniqueId"),
    DEFAULT("")
}
