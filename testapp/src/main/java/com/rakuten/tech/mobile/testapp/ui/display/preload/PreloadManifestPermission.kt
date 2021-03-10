package com.rakuten.tech.mobile.testapp.ui.display.preload

import androidx.annotation.Keep
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType

@Keep
data class PreloadManifestPermission(
    val type: MiniAppCustomPermissionType,
    val isRequired: Boolean,
    val reason: String,
    val shouldDisplay: Boolean
)
