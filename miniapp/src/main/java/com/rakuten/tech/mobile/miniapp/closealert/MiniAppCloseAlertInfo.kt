package com.rakuten.tech.mobile.miniapp.closealert

import androidx.annotation.Keep

@Keep
data class MiniAppCloseAlertInfo(
    var shouldDisplay: Boolean = false,
    var title: String? = "",
    var description: String? = ""
)
