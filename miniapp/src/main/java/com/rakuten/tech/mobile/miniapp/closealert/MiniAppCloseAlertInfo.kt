package com.rakuten.tech.mobile.miniapp.closealert

import androidx.annotation.Keep

/** An object to prepare information of close alert popup. */
@Keep
data class MiniAppCloseAlertInfo(
    var shouldDisplay: Boolean = false,
    var title: String = "",
    var description: String = ""
)
