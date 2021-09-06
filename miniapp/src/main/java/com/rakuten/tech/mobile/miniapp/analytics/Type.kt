package com.rakuten.tech.mobile.miniapp.analytics

internal enum class Etype(val value: String) {
    APPEAR("appear"),
    CLICK("click"),
}

internal enum class Actype(val value: String) {
    HOST_LAUNCH("mini_app_host_launch"),
    OPEN("mini_app_open"),
    CLOSE("mini_app_close"),
    SIGNATURE_VALIDATION_SUCCESS("mini_app_signature_validation_success"),
    SIGNATURE_VALIDATION_FAIL("mini_app_signature_validation_fail")
}
