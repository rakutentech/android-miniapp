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
internal data class CustomPermissionCallbackObj(
    var action: String,
    val param: CustomPermission?,
    var id: String
)

@Keep
internal data class CustomPermission(
    val permissions: List<CustomPermissionObj>
)

@Keep
internal data class CustomPermissionObj(
    val name: String,
    val description: String
)
