package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.annotation.Keep

/** Points object for miniapp. */
@Keep
data class Points(
    val standard: Long,
    var term: Long,
    var cash: Long
)
