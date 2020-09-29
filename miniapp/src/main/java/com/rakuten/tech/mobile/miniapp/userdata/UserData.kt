package com.rakuten.tech.mobile.miniapp.userdata

/** Type of user data. **/
enum class UserDataType(val type: String) {
    UNKNOWN("unknown"),
    USER_NAME("username");

    internal companion object {

        internal fun getValue(type: String) = values().find { it.type == type } ?: UNKNOWN
    }
}
