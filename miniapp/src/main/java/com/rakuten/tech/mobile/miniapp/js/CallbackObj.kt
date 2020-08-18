package com.rakuten.tech.mobile.miniapp.js

import androidx.annotation.Keep

@Keep
internal data class CallbackObj(
    var action: String,
    var param: Any?,
    var id: String
)

@Keep
internal data class Permission(val permission: String)

@Keep
internal data class CustomPermission(val permissions: List<String>)
