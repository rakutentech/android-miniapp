package com.rakuten.tech.mobile.miniapp.js.userinfo

import androidx.annotation.Keep

/** Points object for miniapp. */
@Keep
data class Points(
    val standard: Int,
    var term: Int,
    var cash: Int
)
