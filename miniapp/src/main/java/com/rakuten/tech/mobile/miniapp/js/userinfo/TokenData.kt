package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.annotation.Keep

/** Access token object for miniapp. */
@Keep
data class TokenData(
    val token: String,
    val validUntil: Long
)
