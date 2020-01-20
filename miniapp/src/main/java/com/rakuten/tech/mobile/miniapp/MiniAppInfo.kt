package com.rakuten.tech.mobile.miniapp

/**
 * This represents a Mini App entity.
 */
data class MiniAppInfo(

    /**
     * Mini App identifier unique to a mini app.
     */
    val id: String,

    /**
     * Name of the mini app.
     */
    val name: String,

    /**
     * Description associated with the mini app.
     */
    val description: String,

    /**
     * Icon of the mini app, obtainable from the provided data for this resource.
     */
    val icon: String,

    internal val versionId: String,
    internal val files: List<String>
)
