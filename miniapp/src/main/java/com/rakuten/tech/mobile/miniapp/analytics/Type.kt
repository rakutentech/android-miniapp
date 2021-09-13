package com.rakuten.tech.mobile.miniapp.analytics

internal enum class Etype(val value: String) {
    APPEAR("appear"),
    CLICK("click"),
}

internal enum class Actype(val value: String) {
    HOST_LAUNCH("mini_app_host_launch"),
    OPEN("mini_app_open"),
    CLOSE("mini_app_close"),
    GET_UNIQUE_ID("mini_app_get_unique_id"),
    REQUEST_PERMISSION("mini_app_request_permission"),
    REQUEST_CUSTOM_PERMISSIONS("mini_app_request_custom_permissions"),
    SHARE_INFO("mini_app_share_info"),
    LOAD_AD("mini_app_load_ad"),
    SHOW_AD("mini_app_show_ad"),
    GET_USER_NAME("mini_app_get_user_name"),
    GET_PROFILE_PHOTO("mini_app_get_profile_photo"),
    GET_ACCESS_TOKEN("mini_app_get_access_token"),
    GET_POINTS("mini_app_get_points"),
    SET_SCREEN_ORIENTATION("mini_app_set_screen_orientation"),
    GET_CONTACTS("mini_app_get_contacts"),
    SEND_MESSAGE_TO_CONTACT("mini_app_send_message_to_contact"),
    SEND_MESSAGE_TO_CONTACT_ID("mini_app_send_message_to_contact_id"),
    SEND_MESSAGE_TO_MULTIPLE_CONTACTS("mini_app_send_message_to_multiple_contacts"),
    DEFAULT("")
}
