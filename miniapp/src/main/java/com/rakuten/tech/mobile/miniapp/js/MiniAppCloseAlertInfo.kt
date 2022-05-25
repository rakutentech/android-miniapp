package com.rakuten.tech.mobile.miniapp.js

import androidx.annotation.Keep

@Keep
data class MiniAppCloseAlertInfo(
    var shouldDisplay: Boolean,
    var title: String?,
    var description: String?
)
