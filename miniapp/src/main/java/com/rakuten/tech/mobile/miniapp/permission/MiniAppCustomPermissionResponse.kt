package com.rakuten.tech.mobile.miniapp.permission

/**
 * A data class to prepare the json response of custom permissions to be sent from this SDK.
 */
data class MiniAppCustomPermissionResponse(
    val permissions: ArrayList<CustomPermissionResponseObj>
) {
    /**
     * A data class to hold the json elements to be sent as inside the response.
     */
    data class CustomPermissionResponseObj(
        val name: String,
        val isGranted: String
    )
}
