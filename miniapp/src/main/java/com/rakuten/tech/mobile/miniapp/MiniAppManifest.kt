package com.rakuten.tech.mobile.miniapp

import androidx.annotation.Keep
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType

/**
 * A data class to represent data in the mini app's manifest.
 */
@Suppress("DataClassContainsFunctions")
@Keep
data class MiniAppManifest(
    // List of permissions requested by Mini App in their manifest
    val requiredPermissions: List<Pair<MiniAppCustomPermissionType, String>>,
    val optionalPermissions: List<Pair<MiniAppCustomPermissionType, String>>,

    val manifest: Map<String, String>
) {
    /** Returns manifest value as String for the provide `key`. */
    fun getValue(key: String): String? = manifest[key]
}
