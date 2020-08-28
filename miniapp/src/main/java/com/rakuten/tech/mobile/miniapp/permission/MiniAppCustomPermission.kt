package com.rakuten.tech.mobile.miniapp.permission

/**
 * A data class to hold the custom permission with grant results using Pair per MiniApp.
 */
data class MiniAppCustomPermission(
    val miniAppId: String,
    val pairValues: List<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>
)
