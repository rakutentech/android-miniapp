package com.rakuten.tech.mobile.miniapp

import android.net.Uri

/**
 * This represents a Mini App entity.
 */
data class MiniAppInfo(

    /**
     * Mini App identifier unique to a mini app.
     */
    val id: String,

    /**
     * [name] of the mini app.
     */
    val name: String,

    /**
     * [description] of the mini app.
     */
    val description: String,

    /**
     * [icon] of the mini app, obtainable from the provided data for this resource.
     */
    val icon: String,

    internal val versionId: String,
    internal val files: List<String>
)
