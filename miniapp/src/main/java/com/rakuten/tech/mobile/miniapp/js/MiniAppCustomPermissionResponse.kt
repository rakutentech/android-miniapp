package com.rakuten.tech.mobile.miniapp.js

/**
 * A data class to prepare the json response of custom permissions to be sent from this SDK.
 */
data class MiniAppCustomPermissionResponse(
    val permissions: ArrayList<CustomPermissionResponseObj>
) {
    data class CustomPermissionResponseObj(
        val name: String,
        val isGranted: String
    )
}
