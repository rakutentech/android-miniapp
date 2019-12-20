package com.rakuten.mobile.miniapp

import android.net.Uri

/**
 * This represents a Mini App entity
 */
data class MiniAppInfo(
    val name: String,
    val description: String,
    val icon: Uri,
    val appId: String
)
