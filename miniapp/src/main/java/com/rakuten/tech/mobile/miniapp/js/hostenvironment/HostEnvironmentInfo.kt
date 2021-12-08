package com.rakuten.tech.mobile.miniapp.js.hostenvironment

import androidx.annotation.Keep

/** HostEnvironmentInfo object for miniapp. */
@Keep
data class HostEnvironmentInfo(
    val platformVersion: String,
    val hostVersion: String,
    val sdkVersion: String,
    val hostLocale: String
)
