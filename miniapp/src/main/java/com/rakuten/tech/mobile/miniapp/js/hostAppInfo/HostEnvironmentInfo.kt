package com.rakuten.tech.mobile.miniapp.js.hostAppInfo

import androidx.annotation.Keep

@Keep
data class HostEnvironmentInfo(
        val platformVersion: String,
        val hostVersion: String,
        val sdkVersion: String
)
