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
     * Display name of the mini app.
     */
    val displayName: String,

    /**
     * Icon of the mini app, obtainable from the provided data for this resource.
     */
    val icon: String,

    /**
     * Version information of the mini app.
     */
    val version: Version
)

/**
 * This represents a version entity of a Mini App.
 */
data class Version(
    /**
     * Version information of the mini app.
     */
    val versionTag: String,

    /**
     * Version identifier of the mini app.
     */
    val versionId: String
)
