package com.rakuten.tech.mobile.miniapp

import androidx.annotation.Keep
import com.rakuten.tech.mobile.miniapp.permission.AccessTokenScope
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType

/**
 * A data class to represent data in the mini app's manifest.
 * @property requiredPermissions List of required permissions requested by Mini App.
 * @property optionalPermissions List of optional permissions requested by Mini App.
 * @property accessTokenPermissions List of audiences and scopes requested by Mini App.
 * @property customMetaData Custom metadata set by Mini App.
 */
@Keep
data class MiniAppManifest(
    val requiredPermissions: List<Pair<MiniAppCustomPermissionType, String>>,
    val optionalPermissions: List<Pair<MiniAppCustomPermissionType, String>>,
    val accessTokenPermissions: List<AccessTokenScope>,
    val customMetaData: Map<String, String>
)
