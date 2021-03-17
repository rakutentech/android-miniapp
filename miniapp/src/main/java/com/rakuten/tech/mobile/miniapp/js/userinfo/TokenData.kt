package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.annotation.Keep
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenPermission

/** Access token object for miniapp. */
@Keep
data class TokenData(
    val token: String,
    val validUntil: Long,
    var accessTokenPermission: AccessTokenPermission = AccessTokenPermission()
)
