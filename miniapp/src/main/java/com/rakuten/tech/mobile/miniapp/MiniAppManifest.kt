package com.rakuten.tech.mobile.miniapp

import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType

// Model representing data in the mini app's manifest
data class MiniAppManifest(
    // List of permissions requested by Mini App in their manifest
    val requiredPermissions: List<MiniAppCustomPermissionType>?,
    val optionalPermissions: List<MiniAppCustomPermissionType>?,

    // Map containing all key/values from the Mini App manifest
    val manifest: Map<String, String>
) {
    // Returns manifest value as String for the provide `key`
    fun getValue(key: String): String? = manifest[key]
}
